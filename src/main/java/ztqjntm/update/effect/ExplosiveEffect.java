package ztqjntm.update.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ztqjntm.update.HalfExplosionDamageCalculator;

/**
 * TNT 箭：命中方块或实体时爆炸，炸完消失。
 * <p>
 * 半径 4、不点火，实体伤害减半（方块破坏不受影响）。
 * 方块破坏跟随 {@code mobGriefing}：关掉时不炸方块，只保留伤害。
 */
public final class ExplosiveEffect implements ArrowEffect {
    public static final ExplosiveEffect INSTANCE = new ExplosiveEffect();

    /** 爆炸半径，与原版 TNT 一致 */
    private static final float RADIUS = 4.0F;

    private ExplosiveEffect() {
    }

    @Override
    public boolean onHitBlock(ServerLevel level, AbstractArrow arrow, BlockHitResult hit) {
        this.explode(level, arrow, hit.getLocation());
        return true;
    }

    @Override
    public boolean onHitEntity(ServerLevel level, AbstractArrow arrow, EntityHitResult hit) {
        this.explode(level, arrow, hit.getLocation());
        return true;
    }

    private void explode(ServerLevel level, AbstractArrow arrow, Vec3 pos) {
        Level.ExplosionInteraction interaction = level.getGameRules().get(GameRules.MOB_GRIEFING)
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;
        level.explode(
                arrow,
                Explosion.getDefaultDamageSource(level, arrow),
                HalfExplosionDamageCalculator.INSTANCE,
                pos.x,
                pos.y,
                pos.z,
                RADIUS,
                false,
                interaction
        );
    }
}
