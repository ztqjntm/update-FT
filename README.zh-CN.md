# Update FT

[English](README.md) | **简体中文**

> 制箭台从 1.14 起就是个装饰方块。这个模组给它补上了名字里承诺的那件工作。

右键制箭台打开三格合成：**箭头材料 + 木棍 + 羽毛**。多数配方一次出 4 支（TNT 箭 1 支、红石箭 3 支、珍珠箭 2 支）。另外新增 **9 种箭**——从石箭、铜箭，到会炸的 TNT 箭、能远程通电的红石箭，以及把你自己送出去的珍珠箭。

[![Release](https://img.shields.io/github/v/release/ztqjntm/update-FT?style=flat-square&label=release)](https://github.com/ztqjntm/update-FT/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-3C8527?style=flat-square)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Fabric-DBB69B?style=flat-square)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MPL--2.0-blue?style=flat-square)](LICENSE)
[![build](https://github.com/ztqjntm/update-FT/actions/workflows/build.yml/badge.svg)](https://github.com/ztqjntm/update-FT/actions/workflows/build.yml)

<!--
  顶部横幅（可选但建议）：1280×400 的 banner.png 放 docs/images/banner.png，
  有了以后取消下面这行注释。
  ![Update FT](docs/images/banner.png)
-->

## 为什么装它

- **制箭台终于有用了。** 原版它只是个村民工位，现在右键就能合成箭。
- **前期不缺箭。** 没燧石就用圆石、铜锭、铁锭顶上，材料就地取材。
- **九种箭各有各的用法。** 拆家、远程开关红石、位移、引雷，每一种都值得往背包里塞一沓。

## 截图

<!--
  建议放 4 张图（16:9，宽度 ≤1000px），存 docs/images/：
  1. gui.png       制箭台界面 —— 最重要的一张：三格放好材料、产物格有箭
  2. arrows.png    九种箭 + 悬停 tooltip
  3. tnt.png       TNT 箭爆炸瞬间
  4. redstone.png  红石箭点亮红石灯
  换成一段 10 秒 GIF（右键制箭台 → 取走成品）比四张静图更有说服力。
  图片就位后取消下面的注释；补齐之前先把这一节整节删掉。
  ![制箭台界面](docs/images/gui.png)
  ![九种箭](docs/images/arrows.png)
-->

## 安装

1. 安装 **Fabric Loader ≥ 0.19.5**（Minecraft **1.21.11**），需要 Java 21。
2. 把 [Fabric API](https://modrinth.com/mod/fabric-api) 放进 `mods/`。
3. 把 `update-ft-1.0.0.jar` 放进 `mods/`。

单人游戏这样就够了。**开服的话服务端也要装**——模组会注册方块、物品和菜单，只有客户端装是连不上的。

## 下载

| 渠道 | 版本 | 说明 |
| --- | --- | --- |
| [GitHub Releases](https://github.com/ztqjntm/update-FT/releases) | 1.0.0 | 在 `Assets` 里下 jar |
| [CurseForge](https://www.curseforge.com/minecraft/mc-mods/update-ft) | 1.0.0 | 支持 CurseForge App 一键安装 |
| Modrinth | — | 未上架 |

| 模组版本 | Minecraft | 加载器 |
| --- | --- | --- |
| 1.0.0 | 1.21.11 | Fabric |

## 九种箭

| 箭 | 伤害加成 | 特殊效果 |
| --- | --- | --- |
| 石箭 | +0 | — |
| 铜箭 | +0 | 雷雨天露天命中时召唤一道真实闪电 |
| 铁箭 | +1 | — |
| 金箭 | −1 | — |
| 钻石箭 | +2 | — |
| 下界合金箭 | +4 | — |
| TNT 箭 | −1 | 命中方块或实体时爆炸 |
| 红石箭 | −1 | 命中方块后激发一次红石脉冲 |
| 珍珠箭 | ±0 | 命中后把射手传送到落点 |

伤害加成是**直接加在原版算出的最终伤害上**（原版先按速度和暴击算完、向上取整，再 `+ 加成`，最低 0）。所以钻石箭不是"伤害翻倍"，而是稳定多 2 点。

<details>
<summary>四种特殊箭的细节</summary>

- **TNT 箭**：半径 4 的爆炸，不点燃火焰，**会破坏方块**（相当于一支可回收的 TNT）。实体伤害减半，所以贴脸射自己不至于当场去世。服务器关掉 `mobGriefing` 后只伤人、不拆方块。
- **红石箭**：命中方块后，以落点为中心 2 格触发按钮、拉杆、门、栅栏门、活板门、钟、蜡烛、蜂箱（不破坏方块），同时在命中面外侧那一格放一个隐形方块，**持续 8 tick 输出 15 级信号**——红石灯、活塞、红石粉、发射器、铁轨这类靠信号强度的元件才会真的响应。箭本身照常插进方块，可以捡回来重复用。
  - 只对**空气和水源**通电：雪层、高草、含水海草、流动水这些位置放上去会把原方块吃掉，所以只保留触发效果、不给信号。
  - 同一格再射中只是续时（重置为 8 tick），不会叠出第二个方块。
- **珍珠箭**：把**射手**传送到落点，先沿箭飞来的反方向退 0.6 格，再在 4 格高度内找一个不卡方块的位置；代价是 3 点摔落伤害（原版末影珍珠是 5 点）。箭会被消耗——但**传送不成立时不消耗**（比如发射器射出的箭没有主人），会像普通箭一样插在地上。
- **铜箭**：只有**雷雨天**且落点**露天**才生效，地下和室内就是一支普通箭。落点召唤的是真闪电，会点燃方块，也会劈到你自己和附近的生物；伤害和击杀都算在射手头上。

</details>

## 制箭台

右键制箭台打开界面，三个原料格**位置固定**：右上收箭头材料、中间收木棍、左下收羽毛，放错的东西根本塞不进去。凑齐后产物格出现箭，取走时每个原料扣 1 个。

| 箭头材料 | 产出 | 每次 |
| --- | --- | --- |
| 燧石 | 原版箭 | 4 |
| 圆石 | 石箭 | 4 |
| 铜锭 | 铜箭 | 4 |
| 铁锭 | 铁箭 | 4 |
| 金锭 | 金箭 | 4 |
| 钻石 | 钻石箭 | 4 |
| 下界合金锭 | 下界合金箭 | 4 |
| TNT 方块 | TNT 箭 | 1 |
| 红石 | 红石箭 | 3 |
| 末影珍珠 | 珍珠箭 | 2 |

几个小规矩：

- 产物只能**整份取走**（右键也是整份），不会出现"只拿 1 支却扣掉一整套材料"。
- 关界面时原料**优先退回背包**，背包满了才掉在地上；产物格只是预览，不会掉出来。
- 界面**不保存**：关掉就退回背包，两个人同时开同一张桌子也是各用各的。

## 兼容性与已知限制

- **只支持 Fabric + Minecraft 1.21.11**，Forge / NeoForge 没有计划。
- **配方写死在代码里**：数据包改不了，也没有配方书和 JEI / REI / EMI 支持，得自己试。
- **红石箭的脉冲只作用于命中面相邻的那一格**，想让远处亮灯得靠红石粉把信号传过去。
- **TNT 箭会拆家**，多人服等于无限 TNT，建议配合领地/保护插件使用。
- 制箭台**没有方块实体**：漏斗接不进去，容器不持久化，桌面也不会展示材料。

## 常见问题

**需要 Fabric API 吗？**
需要，没装会直接启动失败。

**服务器要装吗？**
要。模组注册了方块、物品和菜单，客户端和服务端必须都有。

**箭射出去能捡回来吗？**
普通箭和红石箭插在地上都能捡；TNT 箭爆炸后消失，珍珠箭传送成功后消耗。

**会和别的模组冲突吗？**
只改写了原版箭矢的命中逻辑（一处 Mixin），不改其他模组的内容。真遇到冲突，请带上 `latest.log` 和复现步骤开 issue。

**支持 1.20.x / 1.21.1 吗？**
目前只跟 1.21.11，其他版本没有测试也没有排期。

## 从源码构建

```bash
./gradlew build        # Windows 用 gradlew.bat
```

产物在 `build/libs/`：`update-ft-1.0.0.jar`（实际用的）和 `-sources.jar`（源码包）。需要 JDK 21。

目录结构、每个文件干什么、注册与合成流程、发布流程，都在 [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)。

## 反馈

- Bug / 建议：[Issues](https://github.com/ztqjntm/update-FT/issues)
- 更新日志：[CHANGELOG.md](CHANGELOG.md)
- 开发计划：[TODO.md](TODO.md)
- 作者：[Bilibili](https://space.bilibili.com/3546940667071250)

## 许可

[Mozilla Public License 2.0](LICENSE)。可以自由使用、修改、再分发；修改过的 MPL 文件需要以同样的许可开源。

<details>
<summary>技术信息</summary>

| 项目 | 值 |
| --- | --- |
| 模组 ID | `update-ft` |
| 版本 | 1.0.0 |
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| Java | 21 |
| 包名 | `ztqjntm.update` |
| 环境 | 客户端 + 服务端（`*`） |
| 许可 | MPL-2.0 |

</details>
