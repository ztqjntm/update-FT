# update-ft

给原版**制箭台**加一个可交互界面，并新增 **8 种箭**的 Fabric 模组。

| 项目 | 值 |
| --- | --- |
| 模组 ID | `update-ft` |
| 版本 | 1.0.0 |
| Minecraft | 1.21.11 |
| 加载器 | Fabric Loader ≥ 0.19.5 |
| 依赖 | Fabric API `0.141.6+1.21.11` |
| Java | 21 |
| 包名 | `ztqjntm.update` |
| 环境 | 客户端 + 服务端（`*`） |
| 许可 | MPL-2.0 |

## 功能

### 1. 新增箭矢

全部继承 `ArrowItem`，可被弓、弩、发射器正常发射，并已加入 `minecraft:arrows` 物品标签。

| 物品 ID | 中文名 | 英文名 | 伤害加成 | 特殊效果 |
| --- | --- | --- | --- | --- |
| `update-ft:stone_arrow` | 石箭 | Stone Arrow | +0 | — |
| `update-ft:copper_arrow` | 铜箭 | Copper Arrow | +0 | 雷雨天命中时召唤闪电 |
| `update-ft:iron_arrow` | 铁箭 | Iron Arrow | +1 | — |
| `update-ft:diamond_arrow` | 钻石箭 | Diamond Arrow | +2 | — |
| `update-ft:gold_arrow` | 金箭 | Golden Arrow | +0 | — |
| `update-ft:netherite_arrow` | 下界合金箭 | Netherite Arrow | +4 | — |
| `update-ft:tnt_arrow` | TNT 箭 | TNT Arrow | -1 | 命中方块或实体时爆炸 |
| `update-ft:redstone_arrow` | 红石箭 | Redstone Arrow | -1 | 命中方块后激发红石脉冲 |
| `update-ft:pearl_arrow` | 珍珠箭 | Pearl Arrow | ±0 | 命中后把射手传送到落点 |

- **伤害加成**：直接平加到原版算出的最终伤害上（原版先按速度/暴击算出伤害并向上取整，再 `+ bonus`，最低 0）。
- **TNT 箭爆炸**：半径 `4.0`、不点燃火焰、`ExplosionInteraction.TNT`（**会破坏方块**，且当成 TNT 爆炸处理）；实体伤害由 `HalfExplosionDamageCalculator` 减半，方块破坏不受影响。
- **红石箭脉冲**：命中方块后做两件事，缺一不可——
  1. **触发**：以命中点为中心、半径 `2` 格激发一次 `TRIGGER_BLOCK` 爆炸，沿用风弹机制触发按钮、拉杆、门、栅栏门、活板门、钟、蜡烛、蜂箱，不破坏方块；
  2. **通电**：在命中面放一个隐形的 `update-ft:redstone_pulse` 方块，持续 `8` tick 输出 15 级红石信号，红石灯、活塞、红石粉、发射器、铁轨这些靠信号强度工作的元件才会真正响应。
  配合 `NoEntityDamageCalculator` 保证对实体零伤害、零击退。箭本身不会消失，会正常插进方块里，可以捡回来重复使用。视觉上会迸出电火花并播放拉杆咔哒声。
- **铜箭引雷**：只有**雷雨天**（`level.isThundering()`）才会生效。命中方块或实体时，在落点召唤一道真实闪电——做法同避雷针引雷（`EntityType.LIGHTNING_BOLT.create` + `setCause(射手)`），伤害与击杀都算在射手头上；非雷雨天就只是一支普通箭。注意它是真闪电：会点燃方块、也会劈到射手自己和附近的生物。
- **珍珠箭传送**：命中方块或实体后，把**射手**传送到落点——先沿箭飞来的反方向退 `0.6` 格，再在 4 格高度内做碰撞检测找一个不卡方块的位置；代价是 `3` 点摔落伤害（原版末影珍珠是 5 点），伴随传送音效与末影粒子；箭本身会被消耗。
- **获取方式**：创造模式物品栏「战斗」页签；生存模式目前**没有配方**，无法合成。
- **悬停介绍**：鼠标停在物品上会显示灰色小字说明，每种箭 2–3 行，文本全部放在语言文件里（`tooltip.update-ft.<物品ID>.N`），改文案不用动代码。

### 2. 制箭台界面

按 `ft-gui-coords` 的设计实现。贴图 `textures/gui/ft_gui.png`（256×256 画布，176×166 的面板画在左上角），界面上共 **40** 个可放物品的位置：

- **3 个原料格**：斜向排布 `(66,17)` / `(48,35)` / `(30,53)`，摆放顺序无所谓（配方是无形的）；
- **1 个产物格**：`(124,35)`，底图是 24×24 的大框、16×16 物品居中，只能取不能放；
- **玩家背包 3×9 + 快捷栏**：`x = 8 + 18c`，`y = 84 / 102 / 120 / 142`。

**合成规则**：箭头材料 + 木棍 + 羽毛 → 对应箭矢。配方写死在 `FletchingRecipes` 里：

| 箭头材料 | 产物 | 每次产出 |
| --- | --- | --- |
| 燧石 | 原版箭 | 4 |
| 圆石 | 石箭 | 4 |
| 铜锭 | 铜箭 | 4 |
| 铁锭 | 铁箭 | 4 |
| 金锭 | 金箭 | 4 |
| 钻石 | 钻石箭 | 4 |
| 下界合金锭 | 下界合金箭 | 4 |
| 火药 | TNT 箭 | 2 |
| 红石 | 红石箭 | 4 |
| 末影珍珠 | 珍珠箭 | 2 |

- 不依赖方块实体：三个原料格是每次打开时临时创建的 `SimpleContainer`，**不持久化**。
- **关界面时和工作台一致**：原料优先塞回玩家背包，只有背包满了才掉在地上（走 `AbstractContainerMenu.clearContainer`）；产物格只是预览，关闭时直接清空，不会掉出来。
- 产物一次只能**整份**取走（右键也只拿整份），避免“只拿 1 个却消耗一整套原料”。
- 配方没有配方书支持，玩家需要自己试（后续可以接数据包）。

## 构建

```bash
./gradlew build          # Windows 下用 gradlew.bat
```

产物：

- `build/libs/update-ft-1.0.0.jar` —— 实际使用的模组文件
- `build/libs/update-ft-1.0.0-sources.jar` —— 源码包

## 目录结构

```
update-ft-template-1.21.11/
├── build.gradle                     # 构建脚本（Loom、依赖、jar 打包）
├── gradle.properties                # 版本号 / MC 版本 / 依赖版本集中配置
├── settings.gradle                  # 插件仓库与项目名
├── gradlew / gradlew.bat            # Gradle Wrapper 启动脚本
├── gradle/wrapper/                  # Wrapper 本体与下载地址
├── .github/workflows/build.yml      # GitHub Actions CI
├── LICENSE                          # MPL-2.0
├── README.md                        # 本文档
└── src/main/
    ├── java/ztqjntm/update/         # Java 源码
    └── resources/                   # 资源与元数据
```

## 文件说明

### 构建与配置

| 文件 | 作用 |
| --- | --- |
| `build.gradle` | 应用 `net.fabricmc.fabric-loom-remap` 插件，声明 Minecraft / Fabric Loader / Fabric API 依赖、Java 21 编译目标，`processResources` 把 `fabric.mod.json` 里的 `${version}` 展开成真实版本号，`jar` 任务把 `LICENSE` 一并打进包里。 |
| `gradle.properties` | 所有版本号的唯一来源：`minecraft_version=1.21.11`、`loader_version=0.19.5`、`loom_version=1.15-SNAPSHOT`、`fabric_api_version`、`version`（模组版本）、`group`（包名前缀）。JVM 内存与并行/配置缓存开关也在这里。 |
| `settings.gradle` | 声明 Fabric Maven / Maven Central / Gradle Plugin Portal 三个插件仓库；`rootProject.name = 'update-ft'` 决定产物 jar 的文件名。 |
| `gradlew`、`gradlew.bat` | Gradle Wrapper 启动脚本（Linux/macOS 与 Windows），保证不装 Gradle 也能构建。 |
| `gradle/wrapper/gradle-wrapper.jar` | Wrapper 引导程序本体。 |
| `gradle/wrapper/gradle-wrapper.properties` | 指定 Gradle 版本（9.7.1）与下载地址，当前指向腾讯云镜像，官方地址被注释掉；换网络环境时改这里。 |
| `.github/workflows/build.yml` | CI：push / PR 时用 JDK 25 跑 `./gradlew build`，把 `build/libs/` 上传为构建产物。 |
| `.gitignore` | 忽略 `build/`、`.gradle/`、IDE 配置等。 |
| `.gitattributes` | 统一换行符，避免 Windows 下 `gradlew` 被改成 CRLF 而无法执行。 |
| `LICENSE` | Mozilla Public License 2.0 全文，构建时会被复制进 jar。 |

### Java 源码（`src/main/java/ztqjntm/update/`）

| 文件 | 作用 |
| --- | --- |
| `UpdateFt.java` | **主入口**（`ModInitializer`）。持有 `MOD_ID = "update-ft"`、日志器和 `id(path)` 工具方法；在 `onInitialize()` 里触发物品与 MenuType 的注册，并注册 `UseBlockCallback`：主手右键制箭台 → 服务端 `player.openMenu(...)` 打开界面。 |
| `HalfExplosionDamageCalculator.java` | 自定义爆炸伤害计算器，单例 `INSTANCE`。覆写 `getEntityDamageAmount`，把实体受到的爆炸伤害乘 `0.5`；**不影响方块破坏和击退**。供 TNT 箭使用。 |
| `NoEntityDamageCalculator.java` | 另一个爆炸伤害计算器：`shouldDamageEntity` 返回 `false`、`getKnockbackMultiplier` 返回 `0`，让爆炸对实体完全无效。供红石箭的“脉冲”使用。 |
| `block/ModBlocks.java` | 方块注册表。目前只有 `update-ft:redstone_pulse`（隐形红石脉冲），无物品形态、无掉落物，只由红石箭临时放置。 |
| `block/RedstonePulseBlock.java` | 红石脉冲方块：不渲染（`RenderShape.INVISIBLE`）、无碰撞、可替换，`getSignal`/`getDirectSignal` 固定返回 15 并声明 `isSignalSource`；`tick()` 里把自己变回空气，实现 8 tick 的“脉冲”。 |
| `item/ModItems.java` | 8 种箭的注册表。`register()` 用 `Item.Properties.setId(ResourceKey)` 绑定 ID 后注册进 `BuiltInRegistries.ITEM`；`initialize()` 把 8 种箭加入创造模式「战斗」页签。数值（伤害加成、是否爆炸）与悬停介绍的语言键都写在这个文件里。 |
| `item/ModArrowItem.java` | 自定义箭物品类，继承 `ArrowItem`，多三个字段：`damageBonus`（伤害加成）、`effect`（命中效果枚举 `NONE` / `EXPLOSIVE` / `REDSTONE`）供 Mixin 读取，`descriptionKeys`（悬停介绍的语言键列表）。覆写的 `appendHoverText` 会把每个键渲染成一行灰色小字。 |
| `menu/ModMenuTypes.java` | 注册 `MenuType<FletchingTableMenu>`，ID 为 `update-ft:fletching_table`；客户端用它来决定打开哪个 `Screen`，服务端用它来反序列化菜单。 |
| `menu/FletchingTableMenu.java` | 制箭台容器逻辑（`AbstractContainerMenu`）：3 个斜向原料格 + 1 个产物格 + 玩家背包（坐标与 `ft_gui.png` 一一对应）；监听原料容器变化重算产物；产物格自定义 `onTake` 消耗原料、`tryRemove` 强制整份取走；`quickMoveStack` 支持 Shift 点击；`removed()` 用 `clearContainer` 把原料放回背包。 |
| `recipe/FletchingRecipe.java` | 单个制箭台配方：3 个 `Ingredient` + 产物，`matches()` 做无序匹配（三格中任意摆放都算）。 |
| `recipe/FletchingRecipes.java` | 配方表（写死在代码里）：箭头材料 + 木棍 + 羽毛 → 对应箭矢，`findResult()` 返回当前三格原料匹配到的产物。 |
| `client/UpdateFtClient.java` | **客户端入口**（`ClientModInitializer`）。调用 `MenuScreens.register` 把 `MenuType` 与 `FletchingTableScreen` 绑定。 |
| `client/FletchingTableScreen.java` | 界面渲染：176×166 布局，背景用 `update-ft:textures/gui/ft_gui.png`（从 256×256 画布的 (0,0) 取 176×166）；`init()` 里把标题居中；`render()` 末尾手动调 `renderTooltip`（1.21.11 的父类已不再自动调用）。 |
| `mixin/AbstractArrowMixin.java` | 箭矢行为的核心 Mixin：① `@ModifyArg` 改 `onHitEntity` 里 `Entity.hurtOrSimulate(DamageSource, float)` 的伤害参数，加上 `damageBonus`；② `onHitBlock` 头部注入，按 `effect` 分派——TNT 箭爆炸并移除，红石箭先做 `TRIGGER_BLOCK` 触发、再在命中面放隐形脉冲方块通电（不取消原版逻辑，箭会照常插进方块）；③ `onHitEntity` 在所有 return 处注入，TNT 箭命中实体时爆炸并移除。所有爆炸、放方块与移除都只在服务端执行，客户端交回原版逻辑，由服务端包同步。 |
| `mixin/ExampleMixin.java` | Fabric 模板自带的示例 Mixin（对 `MinecraftServer.loadLevel` 的空注入），**不产生任何效果**，可安全删除（同时从 `update-ft.mixins.json` 的 `mixins` 数组里去掉）。 |

### 资源与元数据（`src/main/resources/`）

| 文件 | 作用 |
| --- | --- |
| `fabric.mod.json` | 模组元数据：id、名称、描述、作者、图标、许可证、入口点（`main` → `UpdateFt`，`client` → `UpdateFtClient`）、Mixin 配置引用，以及 `depends`（Fabric Loader / Minecraft / Java / Fabric API 版本要求）。 |
| `update-ft.mixins.json` | Mixin 配置文件：指定包名 `ztqjntm.update.mixin`、兼容级别 `JAVA_21`、生效的 Mixin 列表，`injectors.defaultRequire = 1`（注入点找不到就直接报错，避免静默失效）。 |
| `assets/update-ft/icon.png` | 模组列表里显示的图标（128×128）。 |
| `assets/update-ft/items/*.json` | **物品定义**（1.21.4+ 的新格式）：每个文件指定该物品用哪个模型，例如 `{"model": {"type": "minecraft:model", "model": "update-ft:item/stone_arrow"}}`。8 个物品各一份。 |
| `assets/update-ft/models/item/*.json` | **物品模型**：`parent` 为 `minecraft:item/generated`，`layer0` 指向对应贴图。8 个物品各一份。 |
| `assets/update-ft/textures/item/*.png` | **物品贴图**，16×16，9 种箭各一张，文件名与物品 ID 一致。 |
| `assets/update-ft/textures/gui/ft_gui.png` | **制箭台界面贴图**，256×256 画布、面板画在 (0,0) 的 176×166 区域；来自 `ft-gui-coords` 的设计稿，已清掉三格下方的 1px 描边残留。 |
| `assets/update-ft/lang/zh_cn.json` | 简体中文文本：8 个 `item.update-ft.*` 物品名、`container.update-ft.fletching_table`（界面标题），以及 17 条 `tooltip.update-ft.*` 悬停介绍（每行一个键）。 |
| `assets/update-ft/lang/en_us.json` | 英文文本，键名与中文文件一一对应。 |
| `data/minecraft/tags/item/arrows.json` | 把 8 种箭并入原版 `minecraft:arrows` 物品标签，显式写了 `"replace": false`（与默认行为一致，即**合并**而非覆盖），这样弓、弩、发射器都会把它们当作合法弹药。 |

> 新增一种箭需要同时改：`ModItems`（注册 + 悬停介绍键）、`items/`、`models/item/`、`textures/item/`（三份资源同名）、`lang` 两个语言文件（物品名与 `tooltip.update-ft.<id>.N` 介绍行）、`data/minecraft/tags/item/arrows.json`，想让它能合成还要在 `FletchingRecipes` 里加一行。

## 关键流程

**注册**：`UpdateFt.onInitialize()` → `ModItems.initialize()`（触发静态字段注册物品、加入创造页签）→ `ModMenuTypes.initialize()`（触发 `MenuType` 注册）→ 注册右键事件。客户端 `UpdateFtClient.onInitializeClient()` → 绑定 `MenuType` 与 `Screen`。

**开界面与合成**：主手右键制箭台 → 服务端 `UseBlockCallback` 返回 `SUCCESS` 并 `openMenu` → 服务端创建 `FletchingTableMenu` + `SimpleContainer(3)` → 发送 `ClientboundOpenScreenPacket` → 客户端用 `MenuType` 新建菜单与 `FletchingTableScreen`，槽位内容由 `AbstractContainerMenu` 自动同步 → 放入三样原料后 `slotsChanged` 调用 `FletchingRecipes.findResult()` 把产物写进产物格 → 取走产物时消耗每个原料 1 个 → 关闭时 `clearContainer` 把原料放回背包。

**射击命中**：弓/弩/发射器发射 → `ArrowItem.createArrow()` 生成 `Arrow` 实体（`pickupItemStack` 即本模组的箭）→ `AbstractArrowMixin` 通过 `getPickupItemStackOrigin()` 认出箭的类型 → 命中实体时加算伤害、TNT 箭额外爆炸；命中方块时 TNT 箭爆炸并移除，红石箭则以命中点为中心激发一次 `TRIGGER_BLOCK` 爆炸（只触发方块，不破坏、不伤人）后照常插进方块。

## 已知限制 / TODO

- **配方只在制箭台里生效**：普通工作台没有任何本模组的配方，生存模式想造这些箭必须用制箭台。
- **配方写死在代码里**：暂不支持数据包/配方书，改配方要动 `FletchingRecipes.java`。
- **红石箭**的脉冲半径固定为 2 格（只对按钮/拉杆/门这类“可触发”方块），真正通电只作用于**命中面相邻的那一格**；想让远处的红石灯亮，需要命中它本身或让红石粉把信号传导过去。
- `update-ft:redstone_pulse` 是隐形、无碰撞、无掉落的临时方块，正常会在 8 tick 后自动消失；只有在服务器于这 8 tick 内崩溃时，才可能残留在世界里（重进后会看到它，可直接打掉）。
- **TNT 箭会破坏方块**（按设计如此），在多人服相当于可无限使用的拆家工具，且近距离会伤到自己。
- `ExampleMixin` 是模板残留的空注入，可以删掉。
- `loom_version` 用的是 `1.15-SNAPSHOT` 快照版本，长时间后可能因仓库更新导致构建失败，建议锁定固定版本。

## 许可

本项目采用 [Mozilla Public License 2.0](LICENSE)（MPL-2.0）许可：可以自由使用、修改、再分发，修改过的 MPL 文件需要以同样的许可开源。
