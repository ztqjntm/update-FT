package ztqjntm.update.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 箭命中后的特殊行为。
 * <p>
 * 这是三个平台（Fabric / NeoForge / Forge）唯一能共享的一层，所以有两条约束：
 * <ul>
 *     <li>不引 {@code net.fabricmc.*}，只用原版类型；</li>
 *     <li>不假设调用方一定能拿到 {@link ServerLevel}——分派点自己判断，拿不到就干脆不调用。</li>
 * </ul>
 * 返回值表示“这支箭是否应该被消耗”：返回 {@code true} 时，由分派点负责移除箭并取消原版命中流程。
 * 普通箭、红石箭、铜箭都返回 {@code false}，箭照常插在地上可以捡回。
 */
public interface ArrowEffect {

    /**
     * 命中方块。
     *
     * @return {@code true} 表示消耗这支箭（例如 TNT 炸完、珍珠传送成功）
     */
    default boolean onHitBlock(ServerLevel level, AbstractArrow arrow, BlockHitResult hit) {
        return false;
    }

    /**
     * 命中实体。
     *
     * @return {@code true} 表示消耗这支箭
     */
    default boolean onHitEntity(ServerLevel level, AbstractArrow arrow, EntityHitResult hit) {
        return false;
    }
}
