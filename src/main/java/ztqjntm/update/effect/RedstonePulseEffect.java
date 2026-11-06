package ztqjntm.update.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ztqjntm.update.NoEntityDamageCalculator;
import ztqjntm.update.block.ModBlocks;
import ztqjntm.update.block.RedstonePulseBlock;
import ztqjntm.update.block.RedstonePulseBlockEntity;

/**
 * 红石箭：命中方块时激发一次红石脉冲，分两步：
 * <ol>
 *     <li>风弹式“触发”：{@link Explosion.BlockInteraction#TRIGGER_BLOCK} 的爆炸会让按钮、拉杆、门、
 *     栅栏门、活板门、钟、蜡烛、蜂箱这些方块响应；</li>
 *     <li>真正的“通电”：在命中面外侧贴一个隐形的 {@link RedstonePulseBlock}，持续输出 15 级红石信号，
 *     红石灯、活塞、红石粉、发射器、铁轨这些靠信号强度工作的元件才会亮/动作。</li>
 * </ol>
 * 两步缺一不可——风弹那套机制本身并不会给方块通电（原版风弹也点不亮红石灯）。
 * <p>
 * 脉冲方块会把它顶掉的原方块记在方块实体里，所以雪层、高草、含水海草、流动水这些位置也放得进去，
 * 8 tick 后 1:1 还回去；同一格再被射中只续时，不覆盖最初记下的原方块。
 * <p>
 * 箭本身不消耗：命中后照常插进方块，可以捡回来。因此两个命中回调都返回 {@code false}；
 * 命中实体只算普通伤害，不触发脉冲。
 */
public final class RedstonePulseEffect implements ArrowEffect {
    public static final RedstonePulseEffect INSTANCE = new RedstonePulseEffect();

    /** 触发半径，与原版风弹一致 */
    private static final float TRIGGER_RADIUS = 2.0F;

    private RedstonePulseEffect() {
    }

    @Override
    public boolean onHitBlock(ServerLevel level, AbstractArrow arrow, BlockHitResult hit) {
        this.trigger(level, arrow, hit);
        return false;
    }

    private void trigger(ServerLevel level, AbstractArrow arrow, BlockHitResult hitResult) {
        BlockPos hitPos = hitResult.getBlockPos();
        Vec3 center = Vec3.atCenterOf(hitPos);

        // ① 风弹式触发
        new ServerExplosion(
                level,
                arrow,
                Explosion.getDefaultDamageSource(level, arrow),
                NoEntityDamageCalculator.INSTANCE,
                center,
                TRIGGER_RADIUS,
                false,
                Explosion.BlockInteraction.TRIGGER_BLOCK
        ).explode();

        // ② 真正的通电：在箭头命中面外侧那一格放一个隐形脉冲方块（只在能安全还原时放）。
        // 命中方块本身就是按钮/拉杆/门这类“已被爆炸触发过”的元件时跳过，避免门被触发开、
        // 又被通电粘住，松电瞬间又关上（看起来像没反应）。
        if (!respondsToTrigger(level.getBlockState(hitPos))) {
            BlockPos pulsePos = hitPos.relative(hitResult.getDirection());
            BlockState original = level.getBlockState(pulsePos);

            if (original.is(ModBlocks.REDSTONE_PULSE)) {
                // 同格再被射中：只续时，不覆盖最初记下的原方块
                if (level.getBlockEntity(pulsePos) instanceof RedstonePulseBlockEntity pulse) {
                    pulse.extend();
                } else {
                    // 方块在、方块实体不在（只可能是存档异常）：撤掉，免得留下永久的信号源
                    level.removeBlock(pulsePos, false);
                }
            } else if (level.isInWorldBounds(pulsePos) && (original.isAir() || original.canBeReplaced())) {
                // 任何“可替换”的方块都能放：原状态记进方块实体，8 tick 后原样还回去。
                // 之前只敢放空气和水源，是因为没有地方存“原本是什么”。
                boolean waterlogged = original.getFluidState().is(FluidTags.WATER);
                level.setBlock(pulsePos, ModBlocks.REDSTONE_PULSE.defaultBlockState()
                        .setValue(RedstonePulseBlock.WATERLOGGED, waterlogged), Block.UPDATE_ALL);
                if (level.getBlockEntity(pulsePos) instanceof RedstonePulseBlockEntity pulse) {
                    pulse.arm(original);
                } else {
                    // 理论上不会发生；真发生了宁可撤掉，也别留一个永久的 15 级信号源
                    level.removeBlock(pulsePos, false);
                }
            }
        }

        // 红石火花 + 咔哒声，给玩家一个“通电了”的反馈
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 24, 0.6D, 0.6D, 0.6D, 0.02D);
        level.playSound(null, hitPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.4F);
    }

    /** 这些方块会被 {@code TRIGGER_BLOCK} 爆炸直接“触发”，不需要再额外通电 */
    private static boolean respondsToTrigger(BlockState state) {
        Block block = state.getBlock();
        // 能用原版标签的就用标签，别的模组加的门/按钮也能被识别；
        // 拉杆、钟、蜡烛、蜂箱没有对应标签，只能按类型判断
        return state.is(BlockTags.BUTTONS)
                || state.is(BlockTags.DOORS)
                || state.is(BlockTags.TRAPDOORS)
                || state.is(BlockTags.FENCE_GATES)
                || block instanceof LeverBlock
                || block instanceof BellBlock
                || block instanceof CandleBlock
                || block instanceof BeehiveBlock;
    }
}
