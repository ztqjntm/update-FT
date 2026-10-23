package ztqjntm.update.menu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import ztqjntm.update.UpdateFt;

public class ModMenuTypes {
	// MenuType 的构造函数在原版是 protected，
	// Fabric 的 transitive access widener 让它在开发环境可直接 new。
	public static final MenuType<FletchingTableMenu> FLETCHING_TABLE =
			Registry.register(
					BuiltInRegistries.MENU,
					UpdateFt.id("fletching_table"),
					new MenuType<>(FletchingTableMenu::new, FeatureFlags.VANILLA_SET)
			);

	// 和 ModItems.initialize() 一样：空方法，只为触发 static 字段初始化
	public static void initialize() {
	}
}
