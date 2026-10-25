package ztqjntm.update.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ztqjntm.update.recipe.FletchingRecipes;

import java.util.Optional;

/**
 * 制箭台界面。
 * <p>
 * 布局按 ft-gui.png 的设计：3 个斜向排布的原料格 + 1 个产物格，共 40 个可放物品的位置
 * （3 原料 + 1 产物 + 36 玩家背包）。所有坐标都是 16×16 物品区左上角，
 * 直接对应贴图上的位置，换贴图时只需要同步改这里的数字。
 */
public class FletchingTableMenu extends AbstractContainerMenu {
	/** 原料格数量 */
	public static final int INPUT_SIZE = 3;
	/** 产物格在 slots 列表里的下标 */
	private static final int RESULT_INDEX = INPUT_SIZE;
	/** 玩家背包第一个槽的下标 */
	private static final int INV_START = INPUT_SIZE + 1;

	private final Container input;
	private final Container result;

	// 客户端构造器：由 MenuType 的工厂调用，先给一个空容器，随后自动与服务端同步
	public FletchingTableMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(INPUT_SIZE));
	}

	// 服务端构造器：直接持有真实的容器
	public FletchingTableMenu(int containerId, Inventory playerInventory, Container container) {
		super(ModMenuTypes.FLETCHING_TABLE, containerId);
		checkContainerSize(container, INPUT_SIZE);
		this.input = container;
		this.result = new SimpleContainer(1);
		container.startOpen(playerInventory.player);

		// 原料格：斜向 18px 步进（右上 → 中间 → 左下）
		this.addSlot(new Slot(container, 0, 66, 17));
		this.addSlot(new Slot(container, 1, 48, 35));
		this.addSlot(new Slot(container, 2, 30, 53));

		// 产物格：底图是 24×24 的大框，16×16 物品居中
		this.addSlot(new FletchingResultSlot(this.result, 0, 124, 35));

		// 玩家背包 3×9（84 / 102 / 120） + 快捷栏 1×9（142）
		this.addStandardInventorySlots(playerInventory, 8, 84);

		// 原料一有变化就重算产物；只有 SimpleContainer 会通知监听器
		if (container instanceof SimpleContainer simple) {
			simple.addListener(this::slotsChanged);
		}
		this.updateResult();
	}

	public Container getContainer() {
		return this.input;
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == this.input) {
			this.updateResult();
		}
	}

	private void updateResult() {
		this.result.setItem(0, FletchingRecipes.findResult(this.input));
	}

	/** 产物格：不能放东西，取走时消耗三个原料各 1 个 */
	private final class FletchingResultSlot extends Slot {
		private FletchingResultSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		/**
		 * 产物一次只能整份拿走。否则右键只拿 1 个却照样消耗一整套原料，
		 * 玩家会白亏材料（1 份产物 = 1 套原料）。
		 */
		@Override
		public Optional<ItemStack> tryRemove(int amount, int maxAmount, Player player) {
			int count = this.getItem().getCount();
			return super.tryRemove(count, count, player);
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			super.onTake(player, stack);
			for (int i = 0; i < INPUT_SIZE; i++) {
				FletchingTableMenu.this.input.removeItem(i, 1);
			}
			FletchingTableMenu.this.updateResult();
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack clicked = stack.copy();

		if (slotIndex == RESULT_INDEX) {
			// 产物 → 背包；搬完再消耗原料（在 onTake 里处理，并会立刻算下一份产物）
			if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
			// 直接返回：onTake 已经把新的产物放进产物格了，别再把它清掉
			return clicked;
		} else if (slotIndex < INPUT_SIZE) {
			// 原料 → 背包
			if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), false)) {
				return ItemStack.EMPTY;
			}
		} else {
			// 背包 → 原料格
			if (!this.moveItemStackTo(stack, 0, INPUT_SIZE, false)) {
				return ItemStack.EMPTY;
			}
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
		return this.input.stillValid(player);
	}

	/**
	 * 关界面时把原料还给玩家——和工作台一致：优先塞进背包，塞不下才掉在地上，
	 * 而不是像之前那样直接全部扔到地上。产物格只是预览，直接清空。
	 */
	@Override
	public void removed(Player player) {
		super.removed(player);
		this.result.setItem(0, ItemStack.EMPTY);
		this.input.stopOpen(player);

		// 客户端也会回调 removed()，但它不做实际入包，避免刷出幽灵物品
		if (!player.level().isClientSide()) {
			this.clearContainer(player, this.input);
		}
	}
}
