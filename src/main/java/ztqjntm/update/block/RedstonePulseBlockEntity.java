package ztqjntm.update.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 红石脉冲方块的状态：这一格原来是什么（{@link #originalState}）以及还剩几 tick（{@link #ticksLeft}）。
 * <p>
 * 有了它，脉冲就能放到任何“可替换”的方块上（雪层、高草、含水海草、流动水……），
 * 8 tick 后原样还回去，不再是 1.0.0 那种“只敢放空气和水源”的妥协。
 * <p>
 * 计时靠方块实体的 ticker，不用计划刻：计划刻在 1.21.11 里按整个 {@code ScheduledTick} 去重，
 * 续时得先清再排，容易出岔子；ticker 每 tick 减一，读档后也自然接着倒计时。
 */
public class RedstonePulseBlockEntity extends BlockEntity {

    private static final String KEY_ORIGINAL = "original_state";
    private static final String KEY_TICKS = "ticks_left";

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private int ticksLeft = RedstonePulseBlock.DURATION;
    /** 正在执行“到期还原”：此时移除方块是我们自己干的，不该把原方块当掉落物吐出来 */
    private boolean restoring;

    public RedstonePulseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_PULSE, pos, state);
    }

    /** 新放下的脉冲：记下这一格原来是什么，并开始计时 */
    public void arm(BlockState original) {
        this.originalState = original;
        this.ticksLeft = RedstonePulseBlock.DURATION;
        this.setChanged();
    }

    /** 同一格再被射中：只续时，不覆盖最初记下的原方块 */
    public void extend() {
        this.ticksLeft = RedstonePulseBlock.DURATION;
        this.setChanged();
    }

    public BlockState originalState() {
        return this.originalState;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RedstonePulseBlockEntity pulse) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        pulse.ticksLeft--;
        // 每 tick 标脏：存档里记的是“还剩多少”，跨存档/重启也能按真实剩余时间还原，
        // 而不是每次读档都从 8 tick 重新数。只在脉冲存在的 8 tick 内发生，成本可以忽略。
        pulse.setChanged();
        if (pulse.ticksLeft > 0) {
            return;
        }
        pulse.restore(server, pos);
    }

    private void restore(ServerLevel level, BlockPos pos) {
        this.restoring = true;
        level.setBlock(pos, this.originalState, Block.UPDATE_ALL);
    }

    /**
     * 方块被换掉时的收尾。正常到期还原会置 {@link #restoring} 跳过；
     * 其余情况意味着玩家在 8 tick 内往这一格放了方块——原方块不会自己回来了，
     * 就地掉出来，别让它凭空消失。水、高草这类没有物品形态的方块自然什么都掉不出来。
     * <p>
     * 区块卸载不会走这个钩子，所以不会在卸载时往地上丢东西。
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.restoring || !(this.level instanceof ServerLevel server)) {
            return;
        }
        ItemStack drop = new ItemStack(this.originalState.getBlock().asItem());
        if (!drop.isEmpty()) {
            Block.popResource(server, pos, drop);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(KEY_ORIGINAL, BlockState.CODEC, this.originalState);
        output.putInt(KEY_TICKS, this.ticksLeft);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.originalState = input.read(KEY_ORIGINAL, BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        this.ticksLeft = input.getIntOr(KEY_TICKS, RedstonePulseBlock.DURATION);
        // 读档时已经到期（存档里剩下 0 或负数）：下一 tick 立刻还原，不留一个永久的 15 级信号源
        if (this.ticksLeft <= 0) {
            this.ticksLeft = 1;
        }
    }
}
