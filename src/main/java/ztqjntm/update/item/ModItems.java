package ztqjntm.update.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import ztqjntm.update.UpdateFt;

import java.util.function.Function;

public class ModItems {
    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, UpdateFt.id(name));
        T item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    // 参数依次是：物品 ID、伤害加成、命中效果、悬停介绍的语言文件 key（每个 key 一行）
    public static final Item STONE_ARROW = register("stone_arrow",
            props -> new ModArrowItem(props, 0, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.stone_arrow.1",
                    "tooltip.update-ft.stone_arrow.2"),
            new Item.Properties());

    public static final Item COPPER_ARROW = register("copper_arrow",
            props -> new ModArrowItem(props, 0, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.copper_arrow.1",
                    "tooltip.update-ft.copper_arrow.2"),
            new Item.Properties());

    public static final Item IRON_ARROW = register("iron_arrow",
            props -> new ModArrowItem(props, 1, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.iron_arrow.1",
                    "tooltip.update-ft.iron_arrow.2"),
            new Item.Properties());

    public static final Item DIAMOND_ARROW = register("diamond_arrow",
            props -> new ModArrowItem(props, 2, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.diamond_arrow.1",
                    "tooltip.update-ft.diamond_arrow.2"),
            new Item.Properties());

    public static final Item GOLD_ARROW = register("gold_arrow",
            props -> new ModArrowItem(props, 0, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.gold_arrow.1",
                    "tooltip.update-ft.gold_arrow.2"),
            new Item.Properties());

    public static final Item NETHERITE_ARROW = register("netherite_arrow",
            props -> new ModArrowItem(props, 4, ModArrowItem.Effect.NONE,
                    "tooltip.update-ft.netherite_arrow.1",
                    "tooltip.update-ft.netherite_arrow.2"),
            new Item.Properties());

    public static final Item TNT_ARROW = register("tnt_arrow",
            props -> new ModArrowItem(props, -1, ModArrowItem.Effect.EXPLOSIVE,
                    "tooltip.update-ft.tnt_arrow.1",
                    "tooltip.update-ft.tnt_arrow.2",
                    "tooltip.update-ft.tnt_arrow.3"),
            new Item.Properties());

    public static final Item REDSTONE_ARROW = register("redstone_arrow",
            props -> new ModArrowItem(props, -1, ModArrowItem.Effect.REDSTONE,
                    "tooltip.update-ft.redstone_arrow.1",
                    "tooltip.update-ft.redstone_arrow.2",
                    "tooltip.update-ft.redstone_arrow.3"),
            new Item.Properties());

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
                .register((itemGroup) -> {
                    itemGroup.accept(ModItems.STONE_ARROW);
                    itemGroup.accept(ModItems.COPPER_ARROW);
                    itemGroup.accept(ModItems.IRON_ARROW);
                    itemGroup.accept(ModItems.DIAMOND_ARROW);
                    itemGroup.accept(ModItems.GOLD_ARROW);
                    itemGroup.accept(ModItems.NETHERITE_ARROW);
                    itemGroup.accept(ModItems.TNT_ARROW);
                    itemGroup.accept(ModItems.REDSTONE_ARROW);
                });
    }
}
