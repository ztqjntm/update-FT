package ztqjntm.update.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * 珍珠箭：把射手传送到落点，效果参考原版末影珍珠
 * （落点位置 + 3 点摔落伤害 + 传送音效/粒子），箭本身被消耗。
 * <p>
 * 传送不成立时（没有主人、主人已经退出、传送被拒绝）不消耗这支箭——
 * 箭会像普通箭一样插在地上，可以捡回来。
 */
public final class TeleportEffect implements ArrowEffect {
    public static final TeleportEffect INSTANCE = new TeleportEffect();

    /** 摔落伤害（原版末影珍珠是 5 点，这里稍微轻一些） */
    private static final float FALL_DAMAGE = 3.0F;
    /** 沿箭飞来的反方向后退的距离，避免整个人嵌进被打中的墙里 */
    private static final double BACK_OFF = 0.6D;

    private TeleportEffect() {
    }

    @Override
    public boolean onHitBlock(ServerLevel level, AbstractArrow arrow, BlockHitResult hit) {
        return this.teleportOwner(level, arrow, hit.getLocation());
    }

    @Override
    public boolean onHitEntity(ServerLevel level, AbstractArrow arrow, EntityHitResult hit) {
        return this.teleportOwner(level, arrow, hit.getLocation());
    }

    /**
     * @return 真的传送了才返回 {@code true}；没有主人（发射器射出的）、主人已经退出、
     *         或传送被拒绝时返回 {@code false}，调用方据此决定要不要消耗这支箭
     */
    private boolean teleportOwner(ServerLevel level, AbstractArrow arrow, Vec3 hit) {
        Entity owner = arrow.getOwner();
        if (owner == null || owner.isRemoved()) {
            return false;
        }

        Vec3 from = arrow.position();
        Vec3 motion = arrow.getDeltaMovement();
        Vec3 offset = motion.lengthSqr() > 1.0E-7D ? motion.normalize().scale(BACK_OFF) : Vec3.ZERO;
        Vec3 dest = findSafeDestination(level, owner, hit.subtract(offset));

        if (!owner.teleportTo(level, dest.x, dest.y, dest.z, Set.of(), owner.getYRot(), owner.getXRot(), false)) {
            return false;
        }
        // 清掉传送前累积的下落距离，否则边掉边射会被“补算”一次摔落伤害（原版末影珍珠同样会重置）
        owner.resetFallDistance();
        owner.hurtServer(level, level.damageSources().fall(), FALL_DAMAGE);

        level.sendParticles(ParticleTypes.PORTAL, from.x, from.y, from.z, 32, 0.3D, 0.3D, 0.3D, 0.12D);
        level.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 0.9D, dest.z, 48, 0.4D, 0.6D, 0.4D, 0.15D);
        level.playSound(null, dest.x, dest.y, dest.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /** 从上往下找一个不会卡住实体的落点，最多向上挪 3 格 */
    private static Vec3 findSafeDestination(ServerLevel level, Entity entity, Vec3 base) {
        for (int dy = 0; dy <= 3; dy++) {
            Vec3 candidate = base.add(0.0D, dy, 0.0D);
            AABB box = entity.getBoundingBox().move(candidate.subtract(entity.position()));
            if (level.noCollision(entity, box)) {
                return candidate;
            }
        }
        return base.add(0.0D, 1.0D, 0.0D);
    }
}
