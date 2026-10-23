package ztqjntm.update.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ztqjntm.update.HalfExplosionDamageCalculator;
import ztqjntm.update.NoEntityDamageCalculator;
import ztqjntm.update.block.ModBlocks;
import ztqjntm.update.block.RedstonePulseBlock;
import ztqjntm.update.item.ModArrowItem;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @ModifyArg(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 1
    )
    private float updateft$applyArrowDamage(float amount) {
        ModArrowItem arrow = this.updateft$getArrowItem();
        if (arrow == null) {
            return amount;
        }
        return Math.max(0.0F, amount + arrow.getDamageBonus());
    }

    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true)
    private void updateft$onHitBlock(BlockHitResult hitResult, CallbackInfo ci) {
        ModArrowItem arrow = this.updateft$getArrowItem();
        if (arrow == null) {
            return;
        }
        AbstractArrow self = (AbstractArrow) (Object) this;
        ModArrowItem.Effect effect = arrow.getEffect();

        // TNT 箭：原版 TNT 威力的爆炸，炸完消失
        if (effect == ModArrowItem.Effect.EXPLOSIVE) {
            // 只在服务端做爆炸和移除；客户端交给原版 onHitBlock（箭先插在方块上），
            // 随后服务端的移除包会把实体同步掉，两侧不会出现不同步。
            if (self.level().isClientSide()) {
                return;
            }
            this.updateft$explode(self, hitResult.getLocation());
            self.discard();
            ci.cancel();
            return;
        }

        // 红石箭：激发一次红石脉冲，然后照常插进方块（之后还能捡回来）。
        // 这里故意不 cancel，让原版的 onHitBlock 继续执行。
        if (effect == ModArrowItem.Effect.REDSTONE && !self.level().isClientSide()) {
            this.updateft$triggerRedstone(self, hitResult);
        }
    }

    // 用 RETURN 而不是 TAIL：onHitEntity 里有多个 return（末影人、未造成伤害等提前返回），
    // TAIL 只会命中最后一个 return，那些分支就不会爆炸。
    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void updateft$onHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
        ModArrowItem arrow = this.updateft$getArrowItem();
        // 红石箭只在命中方块时触发，打中实体只算普通伤害
        if (arrow == null || arrow.getEffect() != ModArrowItem.Effect.EXPLOSIVE) {
            return;
        }
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        this.updateft$explode(self, hitResult.getLocation());
        self.discard();
    }

    @Unique
    private ModArrowItem updateft$getArrowItem() {
        AbstractArrow self = (AbstractArrow) (Object) this;
        ItemStack stack = self.getPickupItemStackOrigin();
        if (stack != null && stack.getItem() instanceof ModArrowItem arrow) {
            return arrow;
        }
        return null;
    }

    /**
     * TNT 箭的爆炸：半径 4、不点火、按 TNT 规则破坏方块，实体伤害减半。
     * 只由服务端调用。
     */
    @Unique
    private void updateft$explode(AbstractArrow self, Vec3 pos) {
        Level level = self.level();
        level.explode(
                self,
                Explosion.getDefaultDamageSource(level, self),
                HalfExplosionDamageCalculator.INSTANCE,
                pos.x,
                pos.y,
                pos.z,
                4.0F,
                false,
                Level.ExplosionInteraction.TNT
        );
    }

    /**
     * 红石箭的红石脉冲，分两步：
     * <ol>
     *     <li>风弹式“触发”：{@link Explosion.BlockInteraction#TRIGGER_BLOCK} 的爆炸会让按钮、拉杆、门、
     *     栅栏门、活板门、钟、蜡烛、蜂箱这些方块响应；</li>
     *     <li>真正的“通电”：在命中面贴一个隐形的 {@link RedstonePulseBlock}，持续输出 15 级红石信号，
     *     红石灯、活塞、红石粉、发射器、铁轨这些靠信号强度工作的元件才会亮/动作。</li>
     * </ol>
     * 两步缺一不可——风弹那套机制本身并不会给方块通电（原版风弹也点不亮红石灯）。
     * 只由服务端调用。
     */
    @Unique
    private void updateft$triggerRedstone(AbstractArrow self, BlockHitResult hitResult) {
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos hitPos = hitResult.getBlockPos();
        Vec3 center = Vec3.atCenterOf(hitPos);

        // ① 风弹式触发
        new ServerExplosion(
                level,
                self,
                Explosion.getDefaultDamageSource(level, self),
                NoEntityDamageCalculator.INSTANCE,
                center,
                2.0F,
                false,
                Explosion.BlockInteraction.TRIGGER_BLOCK
        ).explode();

        // ② 真正的通电：在箭头命中面的空气格放一个隐形脉冲方块。
        // 命中方块本身就是按钮/拉杆/门这类“已被爆炸触发过”的元件时跳过，避免门被触发开、
        // 又被通电粘住，松电瞬间又关上（看起来像没反应）。
        if (!this.updateft$respondsToTrigger(level.getBlockState(hitPos))) {
            BlockPos pulsePos = hitPos.relative(hitResult.getDirection());
            if (level.isInWorldBounds(pulsePos) && level.getBlockState(pulsePos).isAir()) {
                level.setBlock(pulsePos, ModBlocks.REDSTONE_PULSE.defaultBlockState(), Block.UPDATE_ALL);
                level.scheduleTick(pulsePos, ModBlocks.REDSTONE_PULSE, RedstonePulseBlock.DURATION);
            }
        }

        // 红石火花 + 咔哒声，给玩家一个“通电了”的反馈
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 24, 0.6D, 0.6D, 0.6D, 0.02D);
        level.playSound(null, hitPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.4F);
    }

    /** 这些方块会被 {@code TRIGGER_BLOCK} 爆炸直接“触发”，不需要再额外通电 */
    @Unique
    private boolean updateft$respondsToTrigger(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof DoorBlock
                || block instanceof FenceGateBlock
                || block instanceof TrapDoorBlock
                || block instanceof BellBlock;
    }
}
