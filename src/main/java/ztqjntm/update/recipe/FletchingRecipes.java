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
 * 统一规则：**箭头材料 + 木棍 + 羽毛**，三格随意摆放。
 * 想做新配方，往 {@link #RECIPES} 里加一行即可（目前是写死在代码里的，
 * 以后要支持数据包再换成自定义 RecipeType + JSON）。
 */
public final class FletchingRecipes {
    private static final List<FletchingRecipe> RECIPES = List.of(
            // 原版箭
            new FletchingRecipe(new ItemStack(Items.ARROW, 4),
                    Ingredient.of(Items.FLINT), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            // 本模组的箭
            new FletchingRecipe(new ItemStack(ModItems.STONE_ARROW, 4),
                    Ingredient.of(Items.COBBLESTONE), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.COPPER_ARROW, 4),
                    Ingredient.of(Items.COPPER_INGOT), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.IRON_ARROW, 4),
                    Ingredient.of(Items.IRON_INGOT), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.GOLD_ARROW, 4),
                    Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.DIAMOND_ARROW, 4),
                    Ingredient.of(Items.DIAMOND), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.NETHERITE_ARROW, 4),
                    Ingredient.of(Items.NETHERITE_INGOT), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            // 爆炸/功能箭：产出少一些
            new FletchingRecipe(new ItemStack(ModItems.TNT_ARROW, 2),
                    Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.REDSTONE_ARROW, 4),
                    Ingredient.of(Items.REDSTONE), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER)),
            new FletchingRecipe(new ItemStack(ModItems.PEARL_ARROW, 2),
                    Ingredient.of(Items.ENDER_PEARL), Ingredient.of(Items.STICK), Ingredient.of(Items.FEATHER))
    );

    private FletchingRecipes() {
    }

    /** 找出匹配当前三格原料的产物，没有匹配则返回空栈 */
    public static ItemStack findResult(Container input) {
        for (FletchingRecipe recipe : RECIPES) {
            if (recipe.matches(input)) {
                return recipe.createResult();
            }
        }
        return ItemStack.EMPTY;
    }
}
