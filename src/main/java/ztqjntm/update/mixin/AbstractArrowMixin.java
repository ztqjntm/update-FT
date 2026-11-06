package ztqjntm.update.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ztqjntm.update.item.ModArrowItem;

/**
 * 箭矢与原版的唯一接缝：认出手里这支箭是哪个物品，然后把“命中后干什么”整段交给它的
 * {@link ztqjntm.update.effect.ArrowEffect}。
 * <p>
 * 具体效果、爆炸、红石脉冲、传送、引雷都在 {@code ztqjntm.update.effect} 包里，
 * 这里不碰任何数值，也不做平台相关的调用。
 */
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

    /**
     * 效果返回 {@code true} 表示这支箭该被消耗（TNT 炸完、珍珠传送成功），
     * 此时移除箭并取消原版命中流程；返回 {@code false} 就放手让原版把箭插进方块，还能捡回来。
     */
    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true)
    private void updateft$onHitBlock(BlockHitResult hitResult, CallbackInfo ci) {
        ModArrowItem arrowItem = this.updateft$getArrowItem();
        if (arrowItem == null) {
            return;
        }
        AbstractArrow self = (AbstractArrow) (Object) this;
        // 客户端拿不到 ServerLevel：交回原版（箭先插在方块上），服务端处理完会把实体同步掉。
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        if (arrowItem.effect().onHitBlock(level, self, hitResult)) {
            self.discard();
            ci.cancel();
        }
    }

    // 用 RETURN 而不是 TAIL：onHitEntity 里有多个 return（末影人、未造成伤害等提前返回），
    // TAIL 只会命中最后一个 return，那些分支就不会爆炸。
    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void updateft$onHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
        ModArrowItem arrowItem = this.updateft$getArrowItem();
        if (arrowItem == null) {
            return;
        }
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) {
            return;
        }
        if (arrowItem.effect().onHitEntity(level, self, hitResult)) {
            self.discard();
        }
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
}
