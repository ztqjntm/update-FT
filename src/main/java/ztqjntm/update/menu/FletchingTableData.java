package ztqjntm.update.menu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 制箭台里放着的东西，按坐标存在每个 {@link ServerLevel} 的存档数据里。
 * <p>
 * 1.21.11 的 {@code minecraft:fletching_table} 是普通 {@code Block}（没有专属方块类，也没有方块实体），
 * 所以走“给方块挂 BlockEntity”这条路要么动不了、要么会毁掉村庄 POI。这里改成按坐标存：
 * 打开界面时 {@link #getOrCreate} 拿到同一个容器，关界面不清空，两个玩家自然共享，重启也还在。
 * <p>
 * 注意：{@link SimpleContainer} 的 {@code setItem} / {@code removeItem} 不会自动把这里标脏，
 * 所以每个容器都挂了监听调 {@link #setDirty()}——否则 {@code DimensionDataStorage} 只保存
 * {@code isDirty()} 为 true 的条目，重启就丢。
 */
public class FletchingTableData extends SavedData {

    /** 存档文件名，最终是 {@code <world>/data/update_ft_fletching_tables.dat} */
    private static final String FILE_ID = "update_ft_fletching_tables";

    /** 格子数必须和界面的原料格数量一致 */
    private static final int SLOTS = FletchingTableMenu.INPUT_SIZE;

    /** 一条记录：某个坐标的桌子 + 三个格子里的东西（空栈也会写，保证下标对齐） */
    private record Entry(BlockPos pos, List<ItemStack> items) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(Entry::items)
        ).apply(instance, Entry::new));
    }

    public static final Codec<FletchingTableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("tables").forGetter(FletchingTableData::toEntries)
    ).apply(instance, FletchingTableData::new));

    public static final SavedDataType<FletchingTableData> TYPE =
            new SavedDataType<>(FILE_ID, FletchingTableData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<BlockPos, SimpleContainer> containers = new HashMap<>();

    public FletchingTableData() {
    }

    /** 读档：把每条记录还原成容器，并重新挂上标脏监听 */
    private FletchingTableData(List<Entry> entries) {
        for (Entry entry : entries) {
            SimpleContainer container = new SimpleContainer(SLOTS);
            List<ItemStack> items = entry.items();
            for (int slot = 0; slot < Math.min(items.size(), SLOTS); slot++) {
                container.setItem(slot, items.get(slot));
            }
            this.watch(container);
            this.containers.put(entry.pos(), container);
        }
    }

    /** 每个维度一份 */
    public static FletchingTableData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * 拿到某个坐标的容器，没有就建一个。
     * <p>
     * 如果这个坐标已经不是制箭台了（典型情况：桌子被爆炸或活塞弄掉，没走到
     * {@code PlayerBlockBreakEvents}），先把旧内容物掉在原地再删掉条目——不然它们会一直
     * 留在存档里，玩家再也取不出来。
     */
    public SimpleContainer getOrCreate(ServerLevel level, BlockPos pos) {
        SimpleContainer existing = this.containers.get(pos);

        if (!level.getBlockState(pos).is(Blocks.FLETCHING_TABLE)) {
            if (existing != null) {
                this.dropAndRemove(level, pos);
            }
            // 给一个临时容器：调用方（菜单）可能已经建好了，但它不该被写进存档
            return new SimpleContainer(SLOTS);
        }

        if (existing != null) {
            return existing;
        }

        SimpleContainer created = new SimpleContainer(SLOTS);
        this.watch(created);
        this.containers.put(pos, created);
        this.setDirty();
        return created;
    }

    /** 把某个坐标的内容物掉在原地并删掉条目（拆桌、爆炸后才发现的残条目都走这里） */
    public void dropAndRemove(ServerLevel level, BlockPos pos) {
        SimpleContainer container = this.containers.remove(pos);
        if (container == null) {
            return;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }
        container.clearContent();
        this.setDirty();
    }

    /** 容器一有变化就标脏，否则重启后内容会丢 */
    private void watch(SimpleContainer container) {
        container.addListener(changed -> this.setDirty());
    }

    private List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>();
        this.containers.forEach((pos, container) -> {
            List<ItemStack> items = new ArrayList<>(SLOTS);
            boolean empty = true;
            for (int slot = 0; slot < SLOTS; slot++) {
                ItemStack stack = container.getItem(slot);
                items.add(stack);
                empty &= stack.isEmpty();
            }
            // 空桌子不必写进存档；下次打开会重新建一个空容器
            if (!empty) {
                entries.add(new Entry(pos, items));
            }
        });
        return entries;
    }
}
