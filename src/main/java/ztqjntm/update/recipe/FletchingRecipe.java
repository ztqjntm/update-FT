package ztqjntm.update.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * 制箭台配方：3 个原料 → 1 份产物。
 * <p>
 * 原料不区分摆放位置——三个格子的斜向排布只是视觉设计，玩家把三样东西放进去就行。
 */
public class FletchingRecipe {
    private final List<Ingredient> ingredients;
    private final ItemStack result;

    public FletchingRecipe(ItemStack result, Ingredient... ingredients) {
        this.ingredients = List.of(ingredients);
        this.result = result;
    }

    /** 每次取产物都返回一份新的，避免外部改到配方本体 */
    public ItemStack createResult() {
        return this.result.copy();
    }

    public boolean matches(Container input) {
        if (this.ingredients.size() != input.getContainerSize()) {
            return false;
        }
        boolean[] used = new boolean[this.ingredients.size()];
        for (Ingredient ingredient : this.ingredients) {
            boolean found = false;
            for (int i = 0; i < used.length; i++) {
                if (!used[i] && ingredient.test(input.getItem(i))) {
                    used[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
