package ztqjntm.update;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ztqjntm.update.block.ModBlocks;
import ztqjntm.update.item.ModItems;
import ztqjntm.update.menu.FletchingTableMenu;
import ztqjntm.update.menu.ModMenuTypes;

public class UpdateFt implements ModInitializer {
	public static final String MOD_ID = "update-ft";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.initialize();      // 注册物品
		ModBlocks.initialize();     // 注册方块（红石脉冲）
		ModMenuTypes.initialize();  // 注册 MenuType

		// 右键制箭台 → 打开 GUI（不需要方块实体，也不需要 Mixin）
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			// 事件在客户端和服务端都会触发，只在服务端开界面
			if (level.isClientSide()) {
				return InteractionResult.PASS;
			}
			// 只处理主手：客户端在主手返回 PASS 时会再尝试副手，
			// 不拦的话服务端会连开两次界面（先关再开，界面会闪一下）。
			if (hand != InteractionHand.MAIN_HAND) {
				return InteractionResult.PASS;
			}
			// 事件挂在旁观者检查之前，需要自己过滤
			if (player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			if (!level.getBlockState(pos).is(Blocks.FLETCHING_TABLE)) {
				return InteractionResult.PASS;
			}

			player.openMenu(new MenuProvider() {
				@Override
				public Component getDisplayName() {
					// 这个键必须和 lang 文件里的 container. 那一行完全一致
					return Component.translatable("container.update-ft.fletching_table");
				}

				@Override
				public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
					return new FletchingTableMenu(containerId, inventory,
							new SimpleContainer(FletchingTableMenu.CONTAINER_SIZE));
				}
			});

			// SUCCESS 阻断后续原版处理
			return InteractionResult.SUCCESS;
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
