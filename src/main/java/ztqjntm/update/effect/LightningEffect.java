package ztqjntm.update.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 铜箭：雷雨天、且在能看到天空的位置命中时召唤一道真实闪电，做法和避雷针引雷一致。
 * <p>
 * 不是雷雨天、或落点在地下/室内就什么都不做，箭照常插在地上，所以不消耗。
 * 注意这是真闪电：会点燃方块，也会劈到射手自己和附近的生物。
 */
public final class LightningEffect implements ArrowEffect {
    public static final LightningEffect INSTANCE = new LightningEffect();

    private LightningEffect() {
    }

    @Override
    public boolean onHitBlock(ServerLevel level, AbstractArrow arrow, BlockHitResult hit) {
        this.summon(level, arrow, hit.getLocation());
        return false;
    }

    @Override
    public boolean onHitEntity(ServerLevel level, AbstractArrow arrow, EntityHitResult hit) {
        this.summon(level, arrow, hit.getLocation());
        return false;
    }

    private void summon(ServerLevel level, AbstractArrow arrow, Vec3 hit) {
        if (!level.isThundering()) {
            return;
        }
        BlockPos strikePos = BlockPos.containing(hit);
        // 地下/室内不劈，避免在洞穴里点火、误伤。
        // 查的是命中方块**上方**那一格：命中地面时 strikePos 本身在高度图以下，
        // 直接判它永远看不到天；而 canSeeSkyFromBelowWater 在海拔以下会去查海平面，
        // 海底洞穴也会误判成露天。
        if (!level.canSeeSky(strikePos.above())) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) {
            return;
        }
        bolt.snapTo(Vec3.atBottomCenterOf(strikePos));
        // 让伤害/击杀算在射手头上（发射器射出的没有主人，则为 null）
        if (arrow.getOwner() instanceof ServerPlayer player) {
            bolt.setCause(player);
        }
        level.addFreshEntity(bolt);
    }
}
