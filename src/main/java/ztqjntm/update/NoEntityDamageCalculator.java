package ztqjntm.update;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;

/**
 * 只用来“触发方块”、完全不影响实体的爆炸伤害计算器。
 * 红石箭用它模拟一次红石脉冲：不造成伤害，也不产生击退。
 */
public class NoEntityDamageCalculator extends ExplosionDamageCalculator {
    public static final NoEntityDamageCalculator INSTANCE = new NoEntityDamageCalculator();

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return false;
    }

    @Override
    public float getKnockbackMultiplier(Entity entity) {
        return 0.0F;
    }
}
