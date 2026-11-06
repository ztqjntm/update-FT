package ztqjntm.update.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 红石箭留下的“红石脉冲”。
 * <p>
 * 不渲染、无碰撞、不可获取，唯一作用是像红石块一样对外输出 15 级红石信号，
 * 持续 {@link #DURATION} tick 后把被顶掉的原方块原样还回去。
 * <p>
 * 原方块存在 {@link RedstonePulseBlockEntity} 里，所以它现在可以放在任何“可替换”的方块上
 * （雪层、高草、含水海草、流动水……），不再局限于空气和水源。
 * <p>
 * 带 {@link #WATERLOGGED} 是为了水下也能用：直接在水里放一个非含水方块会把水源吃掉、
 * 留下一个洞；含水状态下水面不变，8 tick 后把原来的流体状态还回去，玩家看不出任何异常。
 */
public class RedstonePulseBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final MapCodec<RedstonePulseBlock> CODEC = simpleCodec(RedstonePulseBlock::new);

    /** 脉冲持续时间（tick） */
    public static final int DURATION = 8;
    /** 放在水里时为 true，结束时恢复成水源 */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public RedstonePulseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    /**
     * 玩家仍然可以照常往这一格放方块（{@code replaceable()} 的效果保留），
     * 但流体不能把它冲掉——否则水下放的脉冲会立刻被水替换，8 tick 根本撑不到。
     */
    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstonePulseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            // 客户端不参与倒计时：还原是服务端的事，客户端等方块更新同步过来
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof RedstonePulseBlockEntity pulse) {
                RedstonePulseBlockEntity.tick(tickLevel, pos, tickState, pulse);
            }
        };
    }
}
