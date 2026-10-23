package ztqjntm.update.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 红石箭留下的“红石脉冲”。
 * <p>
 * 不渲染、无碰撞、不可获取，唯一作用是像红石块一样对外输出 15 级红石信号，
 * 持续 {@link #DURATION} tick 后自己变回空气。
 * 有了它，红石灯、活塞、红石粉、发射器这些靠信号强度工作的元件才会真正响应。
 */
public class RedstonePulseBlock extends Block {
    public static final MapCodec<RedstonePulseBlock> CODEC = simpleCodec(RedstonePulseBlock::new);

    /** 脉冲持续时间（tick） */
    public static final int DURATION = 8;

    public RedstonePulseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
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
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 时间到，恢复成空气（会正常触发周围红石元件的信号更新）
        level.removeBlock(pos, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
