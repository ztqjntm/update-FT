package ztqjntm.update.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class ModArrowItem extends ArrowItem {
    /** 命中后的特殊行为 */
    public enum Effect {
        /** 普通箭 */
        NONE,
        /** 命中方块或实体时爆炸（TNT 箭） */
        EXPLOSIVE,
        /** 命中方块时激发一次红石脉冲（红石箭） */
        REDSTONE,
        /** 命中后把射手传送到落点（珍珠箭） */
        TELEPORT,
        /** 雷雨天命中时召唤闪电（铜箭） */
        LIGHTNING
    }

    private final int damageBonus;
    private final Effect effect;
    /** 悬停时显示的小字介绍，每一项对应语言文件里的一行 */
    private final List<String> descriptionKeys;

    public ModArrowItem(Item.Properties properties, int damageBonus, Effect effect, String... descriptionKeys) {
        super(properties);
        this.damageBonus = damageBonus;
        this.effect = effect;
        this.descriptionKeys = List.of(descriptionKeys);
    }

    public int getDamageBonus() {
        return this.damageBonus;
    }

    public Effect getEffect() {
        return this.effect;
    }

    // 物品栏里把鼠标停在物品上时显示的介绍文字。
    // 每行一个独立的 key，方便翻译时单独调整；建议每个物品不要超过 3 行。
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        for (String key : this.descriptionKeys) {
            tooltip.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }
}
