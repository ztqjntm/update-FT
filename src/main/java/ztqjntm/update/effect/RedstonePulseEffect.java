package ztqjntm.update.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ztqjntm.update.NoEntityDamageCalculator;
import ztqjntm.update.block.ModBlocks;
import ztqjntm.update.block.RedstonePulseBlock;

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
            BlockState pulseState = level.getBlockState(pulsePos);

            if (pulseState.is(ModBlocks.REDSTONE_PULSE)) {
                // 同一格再被射中：续时。不能只再 scheduleTick 一次——计划刻是按整个
                // ScheduledTick（含到期时刻）去重的，旧的不会被覆盖，会先到点把方块清掉。
                // 所以先清掉这一格待执行的方块刻，再排一个完整的持续时间。
                level.getBlockTicks().clearArea(BoundingBox.fromCorners(pulsePos, pulsePos));
                level.scheduleTick(pulsePos, ModBlocks.REDSTONE_PULSE, RedstonePulseBlock.DURATION);
            } else if (level.isInWorldBounds(pulsePos) && canHostPulse(pulseState)) {
                // 只接受空气和纯水水源。脉冲方块 8 tick 后只能还原成空气或水源，
                // 放到雪层、高草、含水海草、流动水上等于把那一格原本的东西吃掉，
                // 所以这些位置宁可不放（只有上面的 TRIGGER_BLOCK 触发）。
                boolean waterlogged = pulseState.is(Blocks.WATER);
                level.setBlock(pulsePos, ModBlocks.REDSTONE_PULSE.defaultBlockState()
                        .setValue(RedstonePulseBlock.WATERLOGGED, waterlogged), Block.UPDATE_ALL);
                level.scheduleTick(pulsePos, ModBlocks.REDSTONE_PULSE, RedstonePulseBlock.DURATION);
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

    /**
     * 脉冲方块只能放在"8 tick 后能原样还回去"的位置：空气，或水源方块。
     * <p>
     * 雪层、高草、含水海草、流动水一律不放——脉冲方块结束时只能变回空气或水源，
     * 放在这些位置上等于把原本的方块吃掉（1.0.0 的取舍；记录原方块的完整还原留给后续的 BlockEntity）。
     */
    private static boolean canHostPulse(BlockState state) {
        return state.isAir() || (state.is(Blocks.WATER) && state.getFluidState().isSource());
    }
}
