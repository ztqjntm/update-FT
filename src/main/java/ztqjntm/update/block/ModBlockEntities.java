package ztqjntm.update.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ztqjntm.update.UpdateFt;

public class ModBlockEntities {

    /**
     * 红石脉冲的方块实体：记录被顶掉的原方块和剩余 tick。
     * <p>
     * 1.21.11 的 {@code BlockEntityType} 构造函数是私有的，用 Fabric 的 builder 创建。
     */
    public static final BlockEntityType<RedstonePulseBlockEntity> REDSTONE_PULSE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            UpdateFt.id("redstone_pulse"),
            FabricBlockEntityTypeBuilder.create(RedstonePulseBlockEntity::new, ModBlocks.REDSTONE_PULSE).build()
    );

    // 和 ModItems.initialize() 一样：空方法，只为触发 static 字段初始化
    public static void initialize() {
    }
}
