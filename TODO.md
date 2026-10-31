# TODO

> 当前版本 **1.0.0**（`gradle.properties`）· Minecraft 1.21.11 / Fabric
> 最后更新：2026-10-31（对应提交 `62782a3`）

优先级：**P0** 阻塞发布 · **P1** 影响体验 · **P2** 技术债 · **P3** 可选特性

---

## P0 · 发布前必须

- 暂无阻塞项。构建、CI、资源一致性（9 套物品资源、33×2 语言键）均通过。
- **建议打 1.0.1**：`5f5f5d3` 之后改了两个玩法数值（金箭 −1、红石箭产出 3）和红石脉冲的水下行为，继续挂在 1.0.0 下会让人分不清版本。

## P1 · 已知问题

| # | 问题 | 位置 | 影响 | 建议 |
| :-- | :-- | :-- | :-- | :-- |
| 1 | 含水脉冲恢复成**水源**，若原位原本是**流动水**（level < 8），可能多出一个水源 | `RedstonePulseBlock.tick()` | 极少数流场场景下水体轻微变化 | 目前可接受；要彻底解决需记录原流体状态（BE 或额外属性） |
| 2 | 发射器射出的珍珠箭没有 owner：不传送，但箭会被 `discard()` | `AbstractArrowMixin.updateft$onHitBlock` | 白耗一支箭，玩家困惑 | 无 owner 时不消耗，或在 tooltip 注明 |
| 3 | 蜡烛 / 蜂箱按 `instanceof` 判定，模组方块（同理的“可触发”方块）仍不兼容 | `updateft$respondsToTrigger` | 模组门/按钮之外的同类方块可能被多贴一个脉冲方块 | 原版没有对应标签，只能等社区标签约定 |
| 4 | tooltip key 缺失时会把原始 key 直接显示给玩家 | `ModArrowItem.appendHoverText` | 翻译漏键时很显眼 | 语言文件已有键一致性检查习惯，保持即可 |

## P2 · 技术债

1. **配方体系不是官方实现**（与设计缺口同源）
   - 现状：`FletchingRecipe` / `FletchingRecipes` 是手写表，没有 `RecipeType` / `RecipeSerializer` / `RecipeInput`，`data/` 下没有 `recipe/`。
   - 后果：`/reload` 无效、数据包改不了、JEI/REI/EMI 查不到、没有配方书。
   - 改造：自定义 `RecipeType` + Serializer + JSON（10 条）+ 客户端同步；「固定栏位 + 木棍/羽毛硬编码」原版 JSON 表达不了，必须自定义 serializer。
2. **没有自动化测试**：`test NO-SOURCE`。最值得补的是 `FletchingRecipes.findResult()` 的匹配矩阵和 `InputSlot.mayPlace` 的栏位过滤（gametest）。
3. **CI 只做 build**：没有 Gradle 缓存，也没有 release / 发布 job。
4. **工作区未纳入版本控制的文件**：`128-icon.png`、`512-icon.png`、`2026-10-30_18.28.23.png`、`update-ft-issues/`。需要决定保留、加 `.gitignore` 还是删除。

## P3 · 可选特性

1. **制箭台方块模型**
   - 评估结论（2026-10-31）：现在没有方块/物品模型、也没有方块实体，世界里的桌子就是原版的。
   - 建议顺序：**C → A**，不要 **B**
     - **C（0.1.x，低风险）**：不加模型，只加反馈——打开界面的音效、合成成功的火花。
     - **A（0.2，高风险高收益）**：自建 `update-ft:fletching_table` 方块（blockstate + 模型 + 物品模型 + loot + 配方 + **村民 POI** + 兼容原版方块），可顺带加 BlockEntity 做桌面展示。
     - **B（不推荐）**：用 `assets/minecraft/...` 覆盖原版资源——会和材质包/其它模组抢文件，且无条件改变所有存档的外观。
2. **BlockEntity**：解决容器不持久、多人不共享、漏斗接不进来、桌面无法展示材料/进度。与 A 一起做最划算。
3. **数据包配方**：与 P2-1 同源，做完顺带支持整合包作者自定义。
4. `fabric.mod.json` 的 `contact` 补 `issues` 字段，指向 GitHub Issues（纯可选）。

## 发布检查清单

- [ ] 版本号更新（`gradle.properties` 的 `version`）
- [ ] 英文简介（Modrinth 用，之前那份已 revert，需要重写两三句即可）
- [ ] Modrinth 字段：Categories `Adventure / Equipment / Game Mechanics`、Environment `Client and server`、Loader `Fabric`、Game version `1.21.11`、License `MPL-2.0`
- [ ] GitHub Release 写 changelog（本轮：金箭/红石箭数值、红石脉冲水下、文档同步）
- [ ] 进游戏实测：雪地红石箭、水下红石箭、金箭伤害、红石箭产出 3

---

## 已关闭（2026-10-30 ~ 31 已修复，勿重复处理）

- 制箭台栏位限定（C1 材料 / C2 木棍 / C3 羽毛），Shift 点击按类型落位
- 产物整份取走：`tryRemove` 强制整份 + `canFitEntirely` 预检（背包放不下就不搬）
- 关界面原料放回背包（`clearContainer`），产物预览直接清空
- 菜单绑定方块坐标：离开 8 格或方块被拆自动关闭
- 蹲下可正常在制箭台上放方块；客户端也返回 `SUCCESS`，不再出现幽灵方块预测
- 铜箭引雷：雷雨 + 露天（`canSeeSky(命中方块上方一格)`）才触发
- 珍珠箭传送后 `resetFallDistance()`
- TNT 箭遵循 `mobGriefing`
- 红石脉冲：可替换方块（雪层/高草/藤蔓/水面）都能放、水下为含水方块、8 tick 后还原
- `respondsToTrigger` 改用原版方块标签，补蜡烛 / 蜂箱
- 配方表惰性初始化，解除与 `ModItems` 的加载顺序耦合
- 清理：`ExampleMixin`、未使用的 `LOGGER`、`getContainer()`、`ModItems` 字段类型收窄
- 构建：`fabric-api >= 0.141.6`、loom 锁定 `1.15.5`、wrapper `retries=3` / `timeout=30000`、CI 改 JDK 21
- 文档：README 箭矢数 8→9、tooltip 17→22、目录树根名、效果枚举补全
