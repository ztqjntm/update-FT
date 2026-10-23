package ztqjntm.update;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;

public class HalfExplosionDamageCalculator extends ExplosionDamageCalculator {
    public static final HalfExplosionDamageCalculator INSTANCE = new HalfExplosionDamageCalculator();

    @Override
    public float getEntityDamageAmount(Explosion explosion, Entity entity, float amount) {
        return super.getEntityDamageAmount(explosion, entity, amount) * 0.5F;
    }
}
