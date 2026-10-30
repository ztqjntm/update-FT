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
import ztqjntm.update.block.ModBlocks;
import ztqjntm.update.item.ModItems;
import ztqjntm.update.menu.FletchingTableMenu;
import ztqjntm.update.menu.ModMenuTypes;

public class UpdateFt implements ModInitializer {
	public static final String MOD_ID = "update-ft";

	@Override
	public void onInitialize() {
		ModItems.initialize();      // 注册物品
		ModBlocks.initialize();     // 注册方块（红石脉冲）
		ModMenuTypes.initialize();  // 注册 MenuType

		// 右键制箭台 → 打开 GUI（不需要方块实体，也不需要 Mixin）
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			// 只处理主手：客户端在主手返回 PASS 时会再尝试副手，
			// 不拦的话服务端会连开两次界面（先关再开，界面会闪一下）。
			if (hand != InteractionHand.MAIN_HAND) {
				return InteractionResult.PASS;
			}
			// 事件挂在旁观者检查之前，需要自己过滤
			if (player.isSpectator()) {
				return InteractionResult.PASS;
			}
			// 蹲下时把交互让给原版，否则玩家没法在制箭台上放方块
			if (player.isShiftKeyDown()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			if (!level.getBlockState(pos).is(Blocks.FLETCHING_TABLE)) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				// 菜单需要记住方块坐标，用它在 stillValid 里判断“走远了 / 方块被拆了”
				player.openMenu(new MenuProvider() {
					@Override
					public Component getDisplayName() {
						// 这个键必须和 lang 文件里的 container. 那一行完全一致
						return Component.translatable("container.update-ft.fletching_table");
					}

					@Override
					public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
						return new FletchingTableMenu(containerId, inventory,
								new SimpleContainer(FletchingTableMenu.INPUT_SIZE), pos);
					}
				});
			}

			// 两边都返回 SUCCESS：客户端不做原版右键预测，
			// 否则手里拿着方块时会先预测一次放置（幽灵方块 + 音效），再被服务端纠正。
			return InteractionResult.SUCCESS;
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
