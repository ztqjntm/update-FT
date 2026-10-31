package ztqjntm.update.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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
 * 持续 {@link #DURATION} tick 后恢复原样（空气，或者原来的水）。
 * <p>
 * 带 {@link #WATERLOGGED} 是为了水下也能用：直接在水里放一个非含水方块会把水源吃掉、
 * 留下一个洞；含水状态下水面不变，8 tick 后把水源还回去，玩家看不出任何异常。
 */
public class RedstonePulseBlock extends Block implements SimpleWaterloggedBlock {
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
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            // 水下：把原来占住的那格水（水源）还回去，避免在水体里留下一个洞
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            // 空气中：恢复成空气（会正常触发周围红石元件的信号更新）
            level.removeBlock(pos, false);
        }
    }
}
