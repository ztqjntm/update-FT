# ft-gui.png · 格子坐标测量

> 面板 **176 × 166**（与原版容器完全同尺寸），整张贴图 **256 × 256**，面板贴在贴图左上角 `(0, 0)`。
> 共 **40** 个可放物品的位置。下文所有 `(x, y)` 都是**格子内 16×16 物品区的左上角**，可以直接照抄进
> `addSlot(new Slot(inv, i, x, y))`；槽的视觉外框是 18×18，左上角为 `(x−1, y−1)`。

## 一、关键尺寸

| 项目 | 数值 | 备注 |
|---|---|---|
| 贴图尺寸 | 256 × 256 | `textures/gui/ft_gui.png` |
| 面板（界面本体） | 176 × 166，位于 (0,0) | `imageWidth=176`、`imageHeight=166` |
| 面板内底色区 | 从 (3,3) 开始，170 × 160 | 外圈 1px 黑描边 + 左上 2px 白高光 + 右下 2px `#555555` 阴影 |
| 单个格子 | 物品区 16×16；槽框 18×18 | 槽框 = 物品位 −1（左上各减 1） |
| 格间步进 | 18 px | 横向、纵向相同 |
| 原料格 | 3 个，斜向（每格右下移 18px） | 见第二节 |
| 产物格 | 24×24 大框（外框 26×26） | 比标准格大 6px |
| 进度箭头 | (92,35) – (111,49)，20 × 15，中心 (101.5, 42) | 见第五节 |

## 二、原料格（3 个，斜向排布）

| 序号 | 位置 | 物品区 x, y | 槽框 x, y | 中心点 |
|---|---|---|---|---|
| C1 | 右上 | **66, 17** | 65, 16 | 74, 25 |
| C2 | 中间 | **48, 35** | 47, 34 | 56, 43 |
| C3 | 左下 | **30, 53** | 29, 52 | 38, 61 |

三格构成 18px 步进的斜线；它们同时也是一张 3×3 网格的对角线：
`x ∈ {30, 48, 66}`、`y ∈ {17, 35, 53}`（原点 30,17）。

## 三、产物格

| 项目 | 数值 |
|---|---|
| 底图里的大框 | (120, 31) 起，24 × 24，外框 (119, 30) 起 26 × 26 |
| 16×16 物品居中放置 | **124, 35**（= 120+4, 31+4） |
| 中心点 | 131.5, 42.5 |

## 四、玩家背包 3×9 + 快捷栏 1×9

`x = 8 + 18 × col`，`y = 84 + 18 × row`；快捷栏单独一行 `y = 142`（与第三行之间留了 4px 空隙，和原版一致）。

| 行 \ 列 | c0 | c1 | c2 | c3 | c4 | c5 | c6 | c7 | c8 |
|---|---|---|---|---|---|---|---|---|---|
| **r0** (y=84) | #4 <br> 8, 84 | #5 <br> 26, 84 | #6 <br> 44, 84 | #7 <br> 62, 84 | #8 <br> 80, 84 | #9 <br> 98, 84 | #10 <br> 116, 84 | #11 <br> 134, 84 | #12 <br> 152, 84 |
| **r1** (y=102) | #13 <br> 8, 102 | #14 <br> 26, 102 | #15 <br> 44, 102 | #16 <br> 62, 102 | #17 <br> 80, 102 | #18 <br> 98, 102 | #19 <br> 116, 102 | #20 <br> 134, 102 | #21 <br> 152, 102 |
| **r2** (y=120) | #22 <br> 8, 120 | #23 <br> 26, 120 | #24 <br> 44, 120 | #25 <br> 62, 120 | #26 <br> 80, 120 | #27 <br> 98, 120 | #28 <br> 116, 120 | #29 <br> 134, 120 | #30 <br> 152, 120 |
| **hotbar** (y=142) | #31 <br> 8, 142 | #32 <br> 26, 142 | #33 <br> 44, 142 | #34 <br> 62, 142 | #35 <br> 80, 142 | #36 <br> 98, 142 | #37 <br> 116, 142 | #38 <br> 134, 142 | #39 <br> 152, 142 |

## 五、进度箭头（如果要画动画进度）

- 包围盒：`(92, 35)` – `(111, 49)`，即 **20 × 15**，中心 `(101.5, 42)`。
- 箭头是**烧进底图**的静态图形。要显示合成进度，建议把这段从底图擦掉（用底色 `#CCCCCC` 填充），
  再把箭头单独存成一张贴图，用 `blit` 按进度裁宽（0–20px）绘制。

## 六、Java 代码

```java
// 1) Menu：坐标照抄即可（1.20.x 写法）
public class FtMenu extends AbstractContainerMenu {
    public FtMenu(int id, Inventory playerInv) {
        super(ModMenus.FT.get(), id);

        // 原料格 3 个（斜向；坐标为 16x16 物品区左上角）
        this.addSlot(new Slot(craft, 0, 66, 17));    // C1 右上
        this.addSlot(new Slot(craft, 1, 48, 35));    // C2 中间
        this.addSlot(new Slot(craft, 2, 30, 53));    // C3 左下

        // 产物格：底图是 24x24 大框，16x16 物品居中
        this.addSlot(new Slot(result, 0, 124, 35));

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        // 快捷栏 1x9
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }
}
```

```java
// 2) Screen：尺寸必须与底图一致
public class FtScreen extends AbstractContainerScreen<FtMenu> {
    private static final ResourceLocation TEX =
            new ResourceLocation("yourmod", "textures/gui/ft_gui.png");   // 1.21+ 用 fromNamespaceAndPath

    public FtScreen(FtMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEX, this.leftPos, this.topPos, 0, 0,
               this.imageWidth, this.imageHeight, 256, 256);
    }
}
```

## 七、几个容易踩的坑

1. **`addSlot` 的 x,y 是物品位，不是槽框位。** 这里 `8, 84` 对应 16×16 物品区左上角，外框在 `7, 83`。
   写错 1px 会整体偏移，物品压到白边上。
2. **产物格比标准格大 6px。** 24×24 的框里放 16×16 物品必须居中到 `124, 35`；若直接用 `120, 31` 会贴着左上角。
   另外原版 hover 高亮固定是 `(x−1, y−1)` 的 18×18，在这个大框里会偏左上，介意的话 override `renderSlotHighlight`。
3. **顶部三格有 1px 描边残留。** `(65,34)` 和 `(47,52)` 各有一段 L 形深色线（画图时拖出来的），
   放大能看到「阶梯」下沿的细线。做 mod 前建议用底色 `#CCCCCC` 擦掉，否则窗口里会出现多余线头。
4. **坐标是相对面板的。** 直接给 `Slot` 用没问题；如果自己画别的东西，记得加 `leftPos` / `topPos`，
   不要用屏幕绝对坐标。
5. **做 2× 高清贴图**（512×512）时，所有数值 ×2，`blit` 的绘制尺寸也要 ×2，
   否则 `blit` 会自动缩放成 176×166 的样子，像素会糊。

## 八、交付文件

| 文件 | 内容 |
|---|---|
| `ft_gui_slot_inspector.html` | 交互式查看器：缩放、悬停读坐标、点击复制 `addSlot` 代码 |
| `ft_gui_slot_map.png` | 4× 标注图，带 16px 标尺与编号 |
| `ft_gui_slots.csv` / `ft_gui_slots.json` | 全部 40 个格子的机读坐标：`place`＝传给 `addSlot` 的值、`area`＝底图灰格、`frame`＝含 1px 描边的外框、`center`＝中心点 |
