package ztqjntm.update.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import ztqjntm.update.item.ModItems;

import java.util.List;

/**
 * 制箭台的配方表。
 * <p>
 * 栏位是固定的，和箭的构造一一对应：
 * <ul>
 *     <li>{@link #TIP_SLOT}=0（界面右上）——箭头材料，随配方变化；</li>
 *     <li>{@link #SHAFT_SLOT}=1（界面中间）——恒为木棍；</li>
 *     <li>{@link #FLETCHING_SLOT}=2（界面左下）——恒为羽毛。</li>
 * </ul>
 * 所以每条配方只需要描述“箭头材料 → 产物”。想做新箭，往 {@link #recipes()} 里加一行即可
 * （目前写死在代码里，以后要支持数据包再换成自定义 RecipeType + JSON）。
 */
public final class FletchingRecipes {
    public static final int TIP_SLOT = 0;
    public static final int SHAFT_SLOT = 1;
    public static final int FLETCHING_SLOT = 2;

    /**
     * 配方表。惰性构建——不在类初始化阶段引用 {@link ModItems}，
     * 避免“谁先被加载”变成一条隐性约束（物品必须在注册表冻结之前完成注册）。
     */
    private static List<FletchingRecipe> recipes;

    private FletchingRecipes() {
    }

    private static List<FletchingRecipe> recipes() {
        if (recipes == null) {
            recipes = List.of(
                    // 原版箭
                    new FletchingRecipe(new ItemStack(Items.ARROW, 4), Ingredient.of(Items.FLINT)),
                    // 本模组的箭
                    new FletchingRecipe(new ItemStack(ModItems.STONE_ARROW, 4), Ingredient.of(Items.COBBLESTONE)),
                    new FletchingRecipe(new ItemStack(ModItems.COPPER_ARROW, 4), Ingredient.of(Items.COPPER_INGOT)),
                    new FletchingRecipe(new ItemStack(ModItems.IRON_ARROW, 4), Ingredient.of(Items.IRON_INGOT)),
                    new FletchingRecipe(new ItemStack(ModItems.GOLD_ARROW, 4), Ingredient.of(Items.GOLD_INGOT)),
                    new FletchingRecipe(new ItemStack(ModItems.DIAMOND_ARROW, 4), Ingredient.of(Items.DIAMOND)),
                    new FletchingRecipe(new ItemStack(ModItems.NETHERITE_ARROW, 4), Ingredient.of(Items.NETHERITE_INGOT)),
                    // 爆炸/功能箭：产出少一些
                    new FletchingRecipe(new ItemStack(ModItems.TNT_ARROW, 1), Ingredient.of(Items.TNT)),
                    new FletchingRecipe(new ItemStack(ModItems.REDSTONE_ARROW, 3), Ingredient.of(Items.REDSTONE)),
                    new FletchingRecipe(new ItemStack(ModItems.PEARL_ARROW, 2), Ingredient.of(Items.ENDER_PEARL))
            );
        }
        return recipes;
    }

    /** 这个物品能不能当某种箭的箭头（箭头格用它做 mayPlace 过滤） */
    public static boolean isTipMaterial(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FletchingRecipe recipe : recipes()) {
            if (recipe.matchesTip(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 找出匹配当前三格原料的产物：中间必须是木棍、左下必须是羽毛，右上按配方表查。
     * 没有匹配则返回空栈。
     */
    public static ItemStack findResult(Container input) {
        if (input.getContainerSize() <= FLETCHING_SLOT) {
            return ItemStack.EMPTY;
        }
        if (!input.getItem(SHAFT_SLOT).is(Items.STICK) || !input.getItem(FLETCHING_SLOT).is(Items.FEATHER)) {
            return ItemStack.EMPTY;
        }
        ItemStack tip = input.getItem(TIP_SLOT);
        for (FletchingRecipe recipe : recipes()) {
            if (recipe.matchesTip(tip)) {
                return recipe.createResult();
            }
        }
        return ItemStack.EMPTY;
    }
}
