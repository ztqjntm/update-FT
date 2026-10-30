package ztqjntm.update.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 一个制箭台配方：**箭头材料**固定放在 C1（右上），C2（中间）恒为木棍、C3（左下）恒为羽毛。
 * <p>
 * 因为栏位是固定的，配方里只需要描述“什么材料能做箭头”，
 * 木棍和羽毛由 {@link FletchingRecipes#findResult} 直接校验，不必重复写进每条配方。
 */
public class FletchingRecipe {
    private final Ingredient tip;
    private final ItemStack result;

    public FletchingRecipe(ItemStack result, Ingredient tip) {
        this.tip = tip;
        this.result = result;
    }

    /** 每次取产物都返回一份新的，避免外部改到配方本体 */
    public ItemStack createResult() {
        return this.result.copy();
    }

    /** 这个物品能不能当这种箭的箭头 */
    public boolean matchesTip(ItemStack stack) {
        return this.tip.test(stack);
    }
}
