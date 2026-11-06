# TODO

优先级：**P1** 影响体验 · **P2** 技术债 · **P3** 可选特性

## P1 · 已知问题

暂无。1.0.0 修掉了两条：

- 流动水被 8 tick 后的脉冲方块改成**水源**（现在只在水源方块和空气上放脉冲，流动水不放）；
- 发射器射出的**珍珠箭没有 owner**，不传送却仍被 `discard()`（现在没真正传走就不消耗，箭留在地上）。

## P2 · 技术债

1. **配方体系不是官方实现**（与设计缺口同源）
   - 现状：`FletchingRecipe` / `FletchingRecipes` 是手写表，没有 `RecipeType` / `RecipeSerializer` / `RecipeInput`，`data/` 下没有 `recipe/`。
   - 后果：`/reload` 无效、数据包改不了、JEI/REI/EMI 查不到、没有配方书。
   - 改造：自定义 `RecipeType` + Serializer + JSON（10 条）+ 客户端同步；「固定栏位 + 木棍/羽毛硬编码」原版 JSON 表达不了，必须自定义 serializer。
2. **没有自动化测试**：`test NO-SOURCE`。最值得补的是 `FletchingRecipes.findResult()` 的匹配矩阵和 `InputSlot.mayPlace` 的栏位过滤（gametest）。
3. **CI 只做 build**：没有 Gradle 缓存，也没有 release / 发布 job。

## P3 · 可选特性

1. **制箭台方块模型**
   - 评估结论（2026-10-31）：制箭台没有方块/物品模型、也没有方块实体，世界里的桌子就是原版的。
     - **A（0.2，高风险高收益）**：自建 `update-ft:fletching_table` 方块（blockstate + 模型 + 物品模型 + loot + 配方 + **村民 POI** + 兼容原版方块），可顺带加 BlockEntity 做桌面展示。
2. **BlockEntity**：容器持久化与多人共享已经在 1.0.1-beta1 用 `SavedData` 解决，剩下的只有漏斗自动化和桌面展示材料/进度——这两件必须有真方块实体，而 1.21.11 的制箭台没有专属方块类，只能等方案 A。
3. **数据包配方**：与 P2-1 同源，做完顺带支持整合包作者自定义。

## 发布检查清单

- [x] 版本号更新（`gradle.properties` 的 `version`）
- [x] 英文简介（Modrinth 用，之前那份已 revert，需要重写两三句即可）
- [ ] Modrinth 字段：Categories `Adventure / Equipment / Game Mechanics`、Environment `Client and server`、Loader `Fabric`、Game version `1.21.11`、License `MPL-2.0`
- [ ] GitHub Release 写 changelog（本轮：金箭/红石箭数值、红石脉冲水下、文档同步）
- [ ] 进游戏实测：雪地红石箭、水下红石箭、金箭伤害、红石箭产出 3

---
