package ztqjntm.update.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import ztqjntm.update.UpdateFt;

/**
 * 本模组用到的物品标签。
 */
public final class ModTags {

    /**
     * 能放进箭头格的物品。
     * <p>
     * 定义在 {@code data/update-ft/tags/item/fletching_tips.json}，数据包可以增删（支持嵌套标签，
     * 例如把 {@code #minecraft:planks} 写进 values），改完 {@code /reload} 即生效，不用改代码。
     * 客户端只查这个标签，不需要额外的配方缓存——标签本身会同步到客户端。
     */
    public static final TagKey<Item> FLETCHING_TIPS =
            TagKey.create(Registries.ITEM, UpdateFt.id("fletching_tips"));

    private ModTags() {
    }
}
