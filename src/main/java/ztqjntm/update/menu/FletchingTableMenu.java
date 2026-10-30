package ztqjntm.update.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import ztqjntm.update.recipe.FletchingRecipes;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 制箭台界面。
 * <p>
 * 布局按 ft_gui.png 的设计：3 个斜向排布的原料格 + 1 个产物格，共 40 个可放物品的位置
 * （3 原料 + 1 产物 + 36 玩家背包）。所有坐标都是 16×16 物品区左上角，
 * 直接对应贴图上的位置，换贴图时只需要同步改这里的数字。
 */
public class FletchingTableMenu extends AbstractContainerMenu {
	/** 原料格数量 */
	public static final int INPUT_SIZE = 3;
	/** 离制箭台多远之后界面自动关闭（方块为单位，8 格） */
	private static final double MAX_DISTANCE_SQR = 8.0D * 8.0D;

	/** 产物格在 slots 列表里的下标 */
	private static final int RESULT_INDEX = INPUT_SIZE;
	/** 玩家背包第一个槽的下标 */
	private static final int INV_START = INPUT_SIZE + 1;

	private final Container input;
	private final Container result;
	/** 服务端才有值，客户端用来跳过距离校验（关不关由服务端决定） */
	private final BlockPos blockPos;
	private final boolean clientSide;
	/** 存一份引用，关界面时好移除，避免容器一直握着菜单 */
	private final ContainerListener inputListener = this::slotsChanged;

	// 客户端构造器：由 MenuType 的工厂调用，先给一个空容器，随后自动与服务端同步
	public FletchingTableMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(INPUT_SIZE), null);
	}

	// 服务端构造器：直接持有真实的容器和方块坐标
	public FletchingTableMenu(int containerId, Inventory playerInventory, Container container, BlockPos blockPos) {
		super(ModMenuTypes.FLETCHING_TABLE, containerId);
		checkContainerSize(container, INPUT_SIZE);
		this.input = container;
		this.result = new SimpleContainer(1);
		this.blockPos = blockPos;
		this.clientSide = playerInventory.player.level().isClientSide();
		container.startOpen(playerInventory.player);

		// 原料格：斜向 18px 步进（右上 → 中间 → 左下），栏位是固定的：
		// 右上放箭头材料、中间只放木棍、左下只放羽毛
		this.addSlot(new InputSlot(container, FletchingRecipes.TIP_SLOT, 66, 17,
				FletchingRecipes::isTipMaterial));
		this.addSlot(new InputSlot(container, FletchingRecipes.SHAFT_SLOT, 48, 35,
				stack -> stack.is(Items.STICK)));
		this.addSlot(new InputSlot(container, FletchingRecipes.FLETCHING_SLOT, 30, 53,
				stack -> stack.is(Items.FEATHER)));

		// 产物格：底图是 24×24 的大框，16×16 物品居中
		this.addSlot(new FletchingResultSlot(this.result, 0, 124, 35));

		// 玩家背包 3×9（84 / 102 / 120） + 快捷栏 1×9（142）
		this.addStandardInventorySlots(playerInventory, 8, 84);

		// 原料一有变化就重算产物；只有 SimpleContainer 会通知监听器
		if (container instanceof SimpleContainer simple) {
			simple.addListener(this.inputListener);
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

	/** 产物以服务端为准；客户端也重算的话，多格同时同步时会出现“闪一下”的中间态 */
	private void updateResult() {
		if (this.clientSide) {
			return;
		}
		this.result.setItem(0, FletchingRecipes.findResult(this.input));
	}

	/** 物品该进哪个原料格；返回 -1 表示三个格子都不收 */
	private static int inputSlotFor(ItemStack stack) {
		if (stack.is(Items.STICK)) {
			return FletchingRecipes.SHAFT_SLOT;
		}
		if (stack.is(Items.FEATHER)) {
			return FletchingRecipes.FLETCHING_SLOT;
		}
		if (FletchingRecipes.isTipMaterial(stack)) {
			return FletchingRecipes.TIP_SLOT;
		}
		return -1;
	}

	/** 背包能不能整份装下这叠东西（避免只搬走一部分，却按整份扣原料） */
	private boolean canFitEntirely(ItemStack stack, int start, int end) {
		int remaining = stack.getCount();
		for (int i = start; i < end && remaining > 0; i++) {
			ItemStack target = this.slots.get(i).getItem();
			if (target.isEmpty()) {
				remaining -= Math.min(remaining, stack.getMaxStackSize());
			} else if (ItemStack.isSameItemSameComponents(target, stack)) {
				int space = target.getMaxStackSize() - target.getCount();
				if (space > 0) {
					remaining -= Math.min(remaining, space);
				}
			}
		}
		return remaining <= 0;
	}

	/** 原料格：只接受自己那一种东西（栏位限定），放错位置直接放不进去 */
	private static final class InputSlot extends Slot {
		private final Predicate<ItemStack> filter;

		private InputSlot(Container container, int slot, int x, int y, Predicate<ItemStack> filter) {
			super(container, slot, x, y);
			this.filter = filter;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return this.filter.test(stack);
		}
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
		 * <p>
		 * {@code maxAmount} 是“光标还能装下多少”：装不下整份就拒绝，
		 * 不然双击收集时会把光标堆叠顶爆（超过 64 个）。
		 */
		@Override
		public Optional<ItemStack> tryRemove(int amount, int maxAmount, Player player) {
			int count = this.getItem().getCount();
			if (maxAmount < count) {
				return Optional.empty();
			}
			return super.tryRemove(count, count, player);
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			super.onTake(player, stack);
			// 记统计/成就（内部会给 ITEM_CRAFTED 计数，并调用 Item#onCraftedBy）
			stack.onCraftedBy(player, stack.getCount());
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
			// 产物 → 背包。先确认背包能整份装下：装不下就什么都不做，
			// 否则只搬走一部分也会按整份扣原料，玩家白亏箭矢。
			if (!this.canFitEntirely(stack, INV_START, this.slots.size())) {
				return ItemStack.EMPTY;
			}
			if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
			// 传 clicked（搬运前那份）而不是 stack：stack 此时已经被搬空，
			// 拿它去记统计会记成 0 个
			slot.onTake(player, clicked);
			// 直接返回：onTake 已经把新的产物放进产物格了，别再把它清掉
			return clicked;
		} else if (slotIndex < INPUT_SIZE) {
			// 原料 → 背包
			if (!this.moveItemStackTo(stack, INV_START, this.slots.size(), false)) {
				return ItemStack.EMPTY;
			}
		} else {
			// 背包 → 原料格：按物品类型放进对应格子。
			// 类型不对、或目标格已有别的东西时什么都不做，不要“退而求其次”塞到别的格子，
			// 否则落点又会变得不可预期。
			int target = inputSlotFor(stack);
			if (target < 0 || !this.moveItemStackTo(stack, target, target + 1, false)) {
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
		if (!this.input.stillValid(player)) {
			return false;
		}
		// 客户端没有坐标，关不关交给服务端判断
		if (this.blockPos == null) {
			return true;
		}
		return player.level().getBlockState(this.blockPos).is(Blocks.FLETCHING_TABLE)
				&& player.distanceToSqr(Vec3.atCenterOf(this.blockPos)) <= MAX_DISTANCE_SQR;
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
		if (this.input instanceof SimpleContainer simple) {
			simple.removeListener(this.inputListener);
		}

		// 客户端也会回调 removed()，但它不做实际入包，避免刷出幽灵物品
		if (!player.level().isClientSide()) {
			this.clearContainer(player, this.input);
		}
	}
}
