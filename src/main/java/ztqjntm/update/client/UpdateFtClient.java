package ztqjntm.update.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import ztqjntm.update.menu.ModMenuTypes;

public class UpdateFtClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Screen 只在客户端存在，必须在这里注册
		MenuScreens.register(ModMenuTypes.FLETCHING_TABLE, FletchingTableScreen::new);
	}
}
