package ztqjntm.update.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FletchingTableMenu extends AbstractContainerMenu {
	public static final int CONTAINER_SIZE = 3 * 3;

	private final Container container;

	// 客户端构造器：由 MenuType 的工厂调用，先给一个空容器，随后自动与服务端同步
	public FletchingTableMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
	}

	// 服务端构造器：直接持有真实的容器
	public FletchingTableMenu(int containerId, Inventory playerInventory, Container container) {
		super(ModMenuTypes.FLETCHING_TABLE, containerId);
		checkContainerSize(container, CONTAINER_SIZE);
		this.container = container;
		container.startOpen(playerInventory.player);

		// 3x3 网格。
		// 坐标对齐工作台背景图：网格起始 (30, 17)，玩家背包 (8, 84)。
		// 换成自绘 UI 时，只要改这两个数值即可。
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				this.addSlot(new Slot(container, x + y * 3, 30 + x * 18, 17 + y * 18));
			}
		}

		// 玩家背包 + 物品栏
		this.addStandardInventorySlots(playerInventory, 8, 84);
	}

	public Container getContainer() {
		return container;
	}

	// Shift 点击逻辑
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack clicked = stack.copy();

		if (slotIndex < container.getContainerSize()) {
			if (!this.moveItemStackTo(stack, container.getContainerSize(), this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, 0, container.getContainerSize(), false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return clicked;
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	// 没有方块实体 = 容器不持久化。
	// 官方教程没覆盖这一步；如果不处理，关界面时里面的物品会凭空消失。
	// 这里在关闭时把物品还给玩家（签名以 IDE 提示为准）。
	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);

		// 客户端也会回调 removed()，而客户端的 container 只是服务端同步过来的副本。
		// 在这里 drop 只会在客户端刷出一堆服务器上并不存在的掉落物（幽灵物品），
		// 真正的掉落交给服务端做。
		if (player.level().isClientSide()) {
			return;
		}

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				container.setItem(i, ItemStack.EMPTY);
				player.drop(stack, false);
			}
		}
	}
}
