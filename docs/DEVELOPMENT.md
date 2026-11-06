# 开发文档

README 面向玩家（`README.md` 英文、`README.zh-CN.md` 中文），这里放实现细节：构建、目录结构、每个文件做什么、关键流程、发布规则。

## 环境

- JDK 21（`build.gradle` 里 `options.release = 21`）
- Gradle 由 Wrapper 提供（`gradle/wrapper/gradle-wrapper.properties` 当前指向 Gradle 9.7.1 的腾讯云镜像，官方地址被注释掉；换网络环境时改这里）

## 构建

```bash
./gradlew build          # Windows 下用 gradlew.bat
```

产物：

- `build/libs/update-ft-1.0.1.jar` —— 实际使用的模组文件
- `build/libs/update-ft-1.0.1-sources.jar` —— 源码包

CI（`.github/workflows/build.yml`）在每次 push / PR 时用 JDK 21 跑一遍 `./gradlew build`，并把 `build/libs/` 上传为构建产物。

## 目录结构

```
update-ft/
├── build.gradle                     # 构建脚本（Loom、依赖、jar 打包）
├── gradle.properties                # 版本号 / MC 版本 / 依赖版本集中配置
├── settings.gradle                  # 插件仓库与项目名
├── gradlew / gradlew.bat            # Gradle Wrapper 启动脚本
├── gradle/wrapper/                  # Wrapper 本体与下载地址
├── .github/workflows/
│   ├── build.yml                    # 每次 push / PR 构建
│   └── release.yml                  # 打 tag 构建并发布 GitHub Release
├── LICENSE                          # MPL-2.0
├── README.md                        # 玩家向说明（英文，主）
├── README.zh-CN.md                  # 玩家向说明（简体中文）
├── CHANGELOG.md                     # 版本更新日志（release 正文来源）
├── TODO.md                          # 开发计划与技术债
├── docs/DEVELOPMENT.md              # 本文档
├── src/main/
│   ├── java/ztqjntm/update/         # 通用代码（入口、物品、效果、菜单、配方、Mixin）
│   └── resources/                   # 资源与元数据
└── src/client/
    └── java/ztqjntm/update/client/  # 仅客户端的代码（入口、Screen）
```

## 文件说明

### 构建与配置

| 文件 | 作用 |
| --- | --- |
| `build.gradle` | 应用 `net.fabricmc.fabric-loom-remap` 插件，声明 Minecraft / Fabric Loader / Fabric API 依赖、Java 21 编译目标；开头把 `mod_version` 显式赋给 `project.version`；`loom { splitEnvironmentSourceSets() }` 把 `main` / `client` 拆成两个源集（`mods { }` 里同时声明两者），`processResources` 把 `fabric.mod.json` 里的 `${version}` 展开成真实版本号，`jar` 任务把 `LICENSE` 一并打进包里。 |
| `gradle.properties` | 所有版本号的唯一来源：`minecraft_version=1.21.11`、`loader_version=0.19.5`、`loom_version=1.15.5`（已锁定 release 版本）、`fabric_api_version`、`mod_version`（模组版本，发布时必须与 tag 一致）、`group`（包名前缀）。JVM 内存与并行/配置缓存开关也在这里。 |
| `settings.gradle` | 声明 Fabric Maven / Maven Central / Gradle Plugin Portal 三个插件仓库；`rootProject.name = 'update-ft'` 决定产物 jar 的文件名。 |
| `gradlew`、`gradlew.bat` | Gradle Wrapper 启动脚本（Linux/macOS 与 Windows），保证不装 Gradle 也能构建。 |
| `gradle/wrapper/gradle-wrapper.jar` | Wrapper 引导程序本体。 |
| `gradle/wrapper/gradle-wrapper.properties` | 指定 Gradle 版本（9.7.1）与下载地址，当前指向腾讯云镜像，官方地址被注释掉；换网络环境时改这里。 |
| `.gitignore` | 忽略 `build/`、`.gradle/`、IDE 配置等。 |
| `.gitattributes` | 统一换行符，避免 Windows 下 `gradlew` 被改成 CRLF 而无法执行。 |
| `LICENSE` | Mozilla Public License 2.0 全文，构建时会被复制进 jar。 |
| `.github/workflows/build.yml` | CI：push / PR 时用 JDK 21 跑 `./gradlew build`，把 `build/libs/` 上传为构建产物。 |
| `.github/workflows/release.yml` | 发布：push `v*` tag（或手动指定 tag）时校验 `mod_version` 与 tag 一致 → 构建 → 校验 jar 内 `fabric.mod.json` 的版本 → 用 CHANGELOG 段落（缺失时用自动生成）创建 GitHub Release，上传主 jar 与 sources jar；版本号带 `-` 时自动标记为预发布。同时并行上传 CurseForge（项目 `1775183`，仓库 Secrets 里需配置 `CURSEFORGE_TOKEN`）。 |

### Java 源码

通用代码在 `src/main/java/ztqjntm/update/`，**客户端专用代码在 `src/client/java/ztqjntm/update/client/`**。开了 `splitEnvironmentSourceSets()` 之后，`main` 源集编译时拿不到任何客户端专用类，服务端误用客户端 API 会直接编译失败；反过来 `client` 可以用 `main` 里的一切。两边的 class 最终仍然打进同一个 jar。

| 文件 | 作用 |
| --- | --- |
| `UpdateFt.java` | **主入口**（`ModInitializer`）。持有 `MOD_ID = "update-ft"`、日志器和 `id(path)` 工具方法；在 `onInitialize()` 里按 物品 → 方块 → 方块实体 → MenuType 的顺序注册，并挂两个事件：`UseBlockCallback`（主手右键制箭台 → 服务端 `openMenu`，容器从 `FletchingTableData` 按坐标取）和 `PlayerBlockBreakEvents.AFTER`（拆桌时把内容物掉在原地并删条目）。 |
| `HalfExplosionDamageCalculator.java` | 自定义爆炸伤害计算器，单例 `INSTANCE`。覆写 `getEntityDamageAmount`，把实体受到的爆炸伤害乘 `0.5`；**不影响方块破坏和击退**。供 TNT 箭使用。 |
| `NoEntityDamageCalculator.java` | 另一个爆炸伤害计算器：`shouldDamageEntity` 返回 `false`、`getKnockbackMultiplier` 返回 `0`，让爆炸对实体完全无效。供红石箭的“脉冲”使用。 |
| `block/ModBlocks.java` | 方块注册表。目前只有 `update-ft:redstone_pulse`（隐形红石脉冲），无物品形态、无掉落物，只由红石箭临时放置。 |
| `block/RedstonePulseBlock.java` | 红石脉冲方块：不渲染（`RenderShape.INVISIBLE`）、无碰撞、可替换，`getSignal`/`getDirectSignal` 固定返回 15 并声明 `isSignalSource`；实现 `EntityBlock`，`newBlockEntity` + `getTicker` 把倒计时交给方块实体（不再用计划刻）。 |
| `block/RedstonePulseBlockEntity.java` | 脉冲的状态：`originalState`（被顶掉的原方块）+ `ticksLeft`。ticker 每 tick 减一，到 0 就 `setBlock` 还原；`preRemoveSideEffects` 处理“8 tick 内被玩家换掉”——把原方块掉在原地，且区块卸载不会误触发。读档时 `ticksLeft <= 0` 会被抬到 1，保证下一 tick 就还原，不留永久信号源。 |
| `block/ModBlockEntities.java` | 方块实体注册表。`BlockEntityType` 在 1.21.11 是私有构造，用 Fabric 的 `FabricBlockEntityTypeBuilder.create(...).build()` 注册 `update-ft:redstone_pulse`。 |
| `item/ModItems.java` | 9 种箭的注册表。`register()` 用 `Item.Properties.setId(ResourceKey)` 绑定 ID 后注册进 `BuiltInRegistries.ITEM`；`initialize()` 把 9 种箭加入创造模式「战斗」页签。数值（伤害加成、命中效果）与悬停介绍的语言键都写在这个文件里。 |
| `item/ModArrowItem.java` | 自定义箭物品类，继承 `ArrowItem`，多三个字段：`damageBonus`（伤害加成）、`effect`（`ArrowEffect` 实例，决定命中后干什么）、`descriptionKeys`（悬停介绍的语言键列表）。覆写的 `appendHoverText` 会把每个键渲染成一行灰色小字。 |
| `effect/ArrowEffect.java` | **命中行为的接口**：`onHitBlock` / `onHitEntity` 都返回 `boolean`，表示“这支箭是否应该被消耗”，默认 `false`。不引 `net.fabricmc.*`、不假设调用方一定拿得到 `ServerLevel`——这是三个平台唯一能共享的一层，1.21.1 / 1.20.1 的移植直接复用它。 |
| `effect/NoneEffect.java` | 普通箭：什么都不做，完全交给原版。 |
| `effect/ExplosiveEffect.java` | TNT 箭：半径 4、不点火、实体伤害减半，`mobGriefing` 关闭时不破坏方块；命中方块或实体都消耗。 |
| `effect/RedstonePulseEffect.java` | 红石箭：`TRIGGER_BLOCK` 触发 + 隐形脉冲方块通电（只放空气/水源，同格续时）；命中实体不触发；不消耗。 |
| `effect/TeleportEffect.java` | 珍珠箭：传送射手 + 3 点摔落伤害；没有主人（发射器）或传送失败时返回 `false`，不消耗。 |
| `effect/LightningEffect.java` | 铜箭：雷雨天且露天命中时引雷，击杀记在射手头上；不消耗。 |
| `menu/ModMenuTypes.java` | 注册 `MenuType<FletchingTableMenu>`，ID 为 `update-ft:fletching_table`；客户端用它来决定打开哪个 `Screen`，服务端用它来反序列化菜单。 |
| `menu/FletchingTableMenu.java` | 制箭台容器逻辑（`AbstractContainerMenu`）：3 个斜向原料格（`InputSlot` 用 `mayPlace` 做栏位限定）+ 1 个产物格 + 玩家背包（坐标与 `ft_gui.png` 一一对应）；监听原料容器变化重算产物；产物格自定义 `onTake` 消耗原料、`tryRemove` 强制整份取走；`quickMoveStack` 按物品类型把 Shift 点击的物品路由到对应原料格。`removed()` 在**最后一个查看者**关闭时用 `clearContainer` 把原料退回背包（装不下的掉在脚下），并删掉存档条目——桌面不留东西。 |
| `menu/FletchingTableData.java` | 制箭台的持久化（`SavedData`）：每个 `ServerLevel` 一份，内部 `Map<BlockPos, SimpleContainer>`，codec 存成 `{"tables":[{pos, items}]}`。`getOrCreate` 按坐标取同一个容器（界面开着时双人共享）；每个容器挂了监听调 `setDirty()`，否则崩溃后内容会丢；`SharedContainer` 用 `startOpen`/`stopOpen` 数着当前有几个人开着，最后一个关的人负责结算；`dropAndRemove` 负责拆桌/爆炸后把内容物掉在原地，`discard` 只删条目（东西已经还给玩家）。 |
| `recipe/FletchingRecipe.java` | 单个制箭台配方：只保存**箭头材料**的 `Ingredient` 与产物（木棍、羽毛是固定栏位，不必重复写），`matchesTip()` 判断某物品能不能当这种箭的箭头。 |
| `recipe/FletchingRecipes.java` | 配方表（写死在代码里）与栏位定义：`TIP_SLOT`/`SHAFT_SLOT`/`FLETCHING_SLOT` 三个常量对应界面右上/中间/左下；`findResult()` 先校验中间是木棍、左下是羽毛，再按右上的材料查产物；`isTipMaterial()` 给箭头格做 `mayPlace` 过滤，**只查 `ModTags.FLETCHING_TIPS` 标签**，不再遍历配方表。 |
| `client/UpdateFtClient.java` | **客户端入口**（`ClientModInitializer`）。调用 `MenuScreens.register` 把 `MenuType` 与 `FletchingTableScreen` 绑定。 |
| `client/FletchingTableScreen.java` | 界面渲染：176×166 布局，背景用 `update-ft:textures/gui/ft_gui.png`（从 256×256 画布的 (0,0) 取 176×166）；`init()` 里把标题居中；`render()` 末尾手动调 `renderTooltip`（1.21.11 的父类已不再自动调用）。 |
| `mixin/AbstractArrowMixin.java` | 箭矢与原版的**唯一接缝**（89 行）：① `@ModifyArg` 改 `onHitEntity` 里 `Entity.hurtOrSimulate(DamageSource, float)` 的伤害参数，加上 `damageBonus`；② `onHitBlock` 头部注入，一行分派——`arrow.effect().onHitBlock(level, self, hit)`；③ `onHitEntity` 在所有 return 处注入，分派到 `onHitEntity(...)`。效果返回 `true` 就移除箭并取消原版流程，返回 `false` 就放手让原版把箭插进方块。客户端拿不到 `ServerLevel`，直接交回原版，由服务端包同步。具体效果全在 `effect/` 包里，Mixin 不碰任何数值。 |

### 资源与元数据（`src/main/resources/`）

| 文件 | 作用 |
| --- | --- |
| `fabric.mod.json` | 模组元数据：id、名称、描述、作者、图标、许可证、入口点（`main` → `UpdateFt`，`client` → `UpdateFtClient`）、Mixin 配置引用，以及 `depends`（Fabric Loader / Minecraft / Java / Fabric API 版本要求）。 |
| `update-ft.mixins.json` | Mixin 配置文件：指定包名 `ztqjntm.update.mixin`、兼容级别 `JAVA_21`、生效的 Mixin 列表，`injectors.defaultRequire = 1`（注入点找不到就直接报错，避免静默失效）。 |
| `assets/update-ft/icon.png` | 模组列表里显示的图标（128×128）。 |
| `assets/update-ft/items/*.json` | **物品定义**（1.21.4+ 的新格式）：每个文件指定该物品用哪个模型，例如 `{"model": {"type": "minecraft:model", "model": "update-ft:item/stone_arrow"}}`。9 个物品各一份。 |
| `assets/update-ft/models/item/*.json` | **物品模型**：`parent` 为 `minecraft:item/generated`，`layer0` 指向对应贴图。9 个物品各一份。 |
| `assets/update-ft/textures/item/*.png` | **物品贴图**，16×16，9 种箭各一张，文件名与物品 ID 一致。 |
| `assets/update-ft/textures/gui/ft_gui.png` | **制箭台界面贴图**，256×256 画布、面板画在 (0,0) 的 176×166 区域；设计稿的坐标表与标注图可在 git 历史（提交 `627e937`）里找到。 |
| `assets/update-ft/lang/zh_cn.json` | 简体中文文本：9 个 `item.update-ft.*` 物品名、`container.update-ft.fletching_table`（界面标题），以及 22 条 `tooltip.update-ft.*` 悬停介绍（每行一个键）。 |
| `assets/update-ft/lang/en_us.json` | 英文文本，键名与中文文件一一对应。 |
| `data/update-ft/tags/item/fletching_tips.json` | **箭头格的白名单**：能放进箭头格的物品。数据包可增删（支持嵌套标签），`/reload` 即生效，不用改代码；默认 10 条与配方表的箭头材料一一对应。 |
| `data/minecraft/tags/item/arrows.json` | 把 9 种箭并入原版 `minecraft:arrows` 物品标签，显式写了 `"replace": false`（与默认行为一致，即**合并**而非覆盖），这样弓、弩、发射器都会把它们当作合法弹药。 |

### 界面坐标

176×166 面板，共 40 个可放物品的位置：

- 3 个原料格：斜向排布 `(66,17)` / `(48,35)` / `(30,53)`，栏位固定
- 1 个产物格：`(124,35)`，底图 24×24、物品 16×16 居中，只能取不能放
- 玩家背包 3×9 + 快捷栏：`x = 8 + 18c`，`y = 84 / 102 / 120 / 142`

## 关键流程

**注册**：`UpdateFt.onInitialize()` → `ModItems.initialize()`（触发静态字段注册物品、加入创造页签）→ `ModMenuTypes.initialize()`（触发 `MenuType` 注册）→ 注册右键事件。客户端 `UpdateFtClient.onInitializeClient()` → 绑定 `MenuType` 与 `Screen`。

**开界面与合成**：主手右键制箭台 → 服务端 `UseBlockCallback` 返回 `SUCCESS` 并 `openMenu` → `FletchingTableData.get(level).getOrCreate(pos)` 取出这个坐标的容器（第一次为空，之后是上次留下的东西）→ 创建 `FletchingTableMenu` → 发送 `ClientboundOpenScreenPacket` → 客户端用 `MenuType` 新建菜单与 `FletchingTableScreen`，槽位内容由 `AbstractContainerMenu` 自动同步 → 放入三样原料后容器监听触发 `FletchingRecipes.findResult()` 把产物写进产物格 → 取走产物时消耗每个原料 1 个 → 关界面把剩下的原料退回背包、桌子清空（还有人开着同一张桌子时先不结算，等最后一个关闭）。

**内容物什么时候离开桌面**：正常关闭界面 → `removed()` 把原料退回玩家并删条目；拆桌 → `PlayerBlockBreakEvents.AFTER` 掉在原地；爆炸/活塞没有事件，由 `getOrCreate` 发现“这个坐标已经不是制箭台”时兜底。

**射击命中**：弓/弩/发射器发射 → `ArrowItem.createArrow()` 生成 `Arrow` 实体（`pickupItemStack` 即本模组的箭）→ `AbstractArrowMixin` 用 `getPickupItemStackOrigin()` 认出是哪个物品 → 伤害走 `@ModifyArg` 加 `damageBonus` → 命中回调一行分派给 `arrow.effect()`，由对应的 `ArrowEffect` 决定做什么以及要不要消耗这支箭：TNT 箭炸完消失、珍珠箭传送成功才消失，红石箭和铜箭照常插在地上可以捡回。

## 新增一种箭

需要同时改：

1. `ModItems` —— 注册物品 + 悬停介绍键 + 数值
2. `assets/update-ft/items/<id>.json`
3. `assets/update-ft/models/item/<id>.json`
4. `assets/update-ft/textures/item/<id>.png`（16×16）
5. `lang/zh_cn.json` 与 `lang/en_us.json` —— 物品名 + `tooltip.update-ft.<id>.N`
6. `data/minecraft/tags/item/arrows.json`
7. `data/update-ft/tags/item/fletching_tips.json` —— 用了新的箭头材料就加进这个标签，否则箭头格放不进去
8. 想让它能合成，再往 `FletchingRecipes` 加一行

只改第 7 步那个标签（数据包也能改，`/reload` 生效）只能让材料放得进箭头格；能做出什么仍由代码里的配方表决定。

## 发布流程

打 `v*` tag 即发布（`.github/workflows/release.yml`）：

1. 改 `gradle.properties` 的 `mod_version`，同步 `CHANGELOG.md` 的 `## [版本]` 段落，提交。
2. 打 tag 并推送：`git tag v1.0.1 && git push origin v1.0.1`。
3. 工作流校验 **tag 去掉 `v` 后与 `mod_version` 完全一致**，不一致直接失败；版本号带 `-` 自动标记为预发布。
4. 构建 → 校验 jar 内 `fabric.mod.json` 的版本 → 用 CHANGELOG 对应段落作为 Release 正文（没有就退回自动生成）→ 上传主 jar 与 sources jar → 并行上传 CurseForge。

发布正文也可以先在网页上手工建 Release 并自己写，工作流只补/更新 jar，不动标题和正文。`workflow_dispatch` 只用于重新发布已有 tag，不会构建任意分支。

CurseForge 的 `CURSEFORGE_TOKEN` 存在仓库 Secrets 里（My Account → API Tokens 生成），**不要提交进仓库**；没配时那一步只 warning 并跳过，不影响 GitHub Release。

## 技术债

配方体系（自定义 `RecipeType` / Serializer / 数据包支持）、自动化测试、BlockEntity 等计划见 [TODO.md](../TODO.md)。
