# Update FT

**English** | [简体中文](README.zh-CN.md)

> The fletching table has been decoration since 1.14. This mod gives it the one job its name promises.

Right-click a fletching table to open a three-slot crafting UI: **tip + stick + feather**. Most recipes yield 4 arrows (TNT yields 1, redstone 3, pearl 2). On top of that, **9 arrow types** — from stone and copper to explosive TNT arrows, redstone-pulsing arrows, and a pearl arrow that teleports *you*.

[![Release](https://img.shields.io/github/v/release/ztqjntm/update-FT?style=flat-square&label=release)](https://github.com/ztqjntm/update-FT/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-3C8527?style=flat-square)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Fabric-DBB69B?style=flat-square)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MPL--2.0-blue?style=flat-square)](LICENSE)
[![build](https://github.com/ztqjntm/update-FT/actions/workflows/build.yml/badge.svg)](https://github.com/ztqjntm/update-FT/actions/workflows/build.yml)

<!--
  Banner (optional but recommended): a 1280×400 banner.png at docs/images/banner.png.
  Uncomment the line below once it exists.
  ![Update FT](docs/images/banner.png)
-->

## Why install it

- **The fletching table finally does something.** In vanilla it is a villager workstation and nothing else. Now it opens a crafting UI.
- **No more arrow shortages.** Out of flint? Cobblestone, copper and iron all work, so you craft from whatever you already have.
- **Nine arrows, nine jobs.** Demolition, remote redstone, self-teleport, lightning — each one is worth carrying on its own.

## Screenshots

<!--
  Four shots (16:9, ≤1000px wide) in docs/images/:
  1. gui.png       The fletching table UI — the important one: three slots filled, output populated
  2. arrows.png    All nine arrows with a tooltip open
  3. tnt.png       A TNT arrow mid-explosion
  4. redstone.png  A redstone arrow lighting up a redstone lamp
  A 10-second GIF (right-click → take the arrows) beats four stills.
  Uncomment once the files exist; delete this whole section until then.
  ![Fletching table UI](docs/images/gui.png)
  ![Nine arrows](docs/images/arrows.png)
-->

## Installation

1. Install **Fabric Loader ≥ 0.19.5** for **Minecraft 1.21.11** (Java 21).
2. Drop [Fabric API](https://modrinth.com/mod/fabric-api) into `mods/`.
3. Drop `update-ft-1.0.0.jar` into `mods/`.

That is all for singleplayer. **For a server, install it on both sides** — the mod registers blocks, items and a menu, so a client-only install cannot join.

## Download

| Source | Version | Notes |
| --- | --- | --- |
| [GitHub Releases](https://github.com/ztqjntm/update-FT/releases) | 1.0.0 | Grab the jar under `Assets` |
| [CurseForge](https://www.curseforge.com/minecraft/mc-mods/update-ft) | 1.0.0 | One-click install via the CurseForge app |
| Modrinth | — | Not listed yet |

| Mod version | Minecraft | Loader |
| --- | --- | --- |
| 1.0.0 | 1.21.11 | Fabric |

## The nine arrows

| Arrow | Damage bonus | Effect |
| --- | --- | --- |
| Stone Arrow | +0 | — |
| Copper Arrow | +0 | Summons real lightning on a thunderstorm hit under open sky |
| Iron Arrow | +1 | — |
| Gold Arrow | −1 | — |
| Diamond Arrow | +2 | — |
| Netherite Arrow | +4 | — |
| TNT Arrow | −1 | Explodes on impact with a block or an entity |
| Redstone Arrow | −1 | Fires a redstone pulse where it lands |
| Pearl Arrow | ±0 | Teleports the shooter to the impact point |

The bonus is added **directly to vanilla's final damage** (vanilla rolls speed and crits, rounds up, then the bonus is applied, floored at 0). So a diamond arrow is not "double damage" — it is a flat +2.

<details>
<summary>Details on the four special arrows</summary>

- **TNT Arrow** — radius 4, no fire, **destroys blocks** (a reusable piece of TNT, essentially). Entity damage is halved, so point-blank shots will not instantly kill you. With `mobGriefing` off it only damages entities and leaves the terrain alone.
- **Redstone Arrow** — on hitting a block, it triggers buttons, levers, doors, fence gates, trapdoors, bells, candles and bee nests within 2 blocks (without breaking anything), and places an invisible block on the face it hit that **emits signal strength 15 for 8 ticks** — that is what redstone lamps, pistons, dust, dispensers and rails actually respond to. The arrow itself sticks in the block as usual and can be picked up again.
  - Only **air and water sources** get powered. Snow layers, tall grass, seagrass and flowing water would be eaten by the pulse block, so those spots only get the trigger, not the signal.
  - Hitting the same spot again just resets the timer; it does not stack a second block.
- **Pearl Arrow** — teleports the **shooter** to the impact point: 0.6 blocks back along the arrow's incoming direction, then a collision check within 4 blocks of height for a spot that is not inside a block. Costs 3 fall damage (a vanilla ender pearl costs 5). The arrow is consumed — **unless the teleport cannot happen** (e.g. a dispenser-fired arrow has no owner), in which case it sticks in the ground like any other arrow.
- **Copper Arrow** — only works during a **thunderstorm** with **open sky** above the impact point; underground or indoors it is an ordinary arrow. The bolt is real: it sets fires and can strike you and nearby mobs. Damage and kills are credited to the shooter.

</details>

## The fletching table

Right-click a fletching table to open the UI. The three input slots are **fixed**: tip top-right, stick in the middle, feather bottom-left. Wrong items simply will not go in. Once all three are filled the output slot shows your arrows, and taking them consumes one of each input.

| Tip material | Output | Per craft |
| --- | --- | --- |
| Flint | Arrow | 4 |
| Cobblestone | Stone Arrow | 4 |
| Copper Ingot | Copper Arrow | 4 |
| Iron Ingot | Iron Arrow | 4 |
| Gold Ingot | Gold Arrow | 4 |
| Diamond | Diamond Arrow | 4 |
| Netherite Ingot | Netherite Arrow | 4 |
| Block of TNT | TNT Arrow | 1 |
| Redstone | Redstone Arrow | 3 |
| Ender Pearl | Pearl Arrow | 2 |

A few rules:

- The output is taken **as a whole batch** (right-click included), so you can never take 1 arrow and pay for a full set of materials.
- Closing the UI **returns inputs to your inventory** first, and only drops them on the ground if the inventory is full. The output slot is a preview and never drops.
- The UI **does not persist**: close it and the contents go back to you. Two players opening the same table get separate containers.

## Compatibility & known limitations

- **Fabric and Minecraft 1.21.11 only.** No Forge / NeoForge plans.
- **Recipes are hardcoded**: no datapack support, no recipe book, no JEI / REI / EMI integration — players have to experiment.
- **The redstone pulse only powers the cell adjacent to the face it hit.** To light a lamp further away, route the signal with dust.
- **TNT arrows grief terrain.** On a multiplayer server that is infinite TNT; pair it with claim/protection plugins.
- **The fletching table has no block entity**: hoppers cannot feed it, containers do not persist, and nothing is displayed on the table itself.

## FAQ

**Do I need Fabric API?**
Yes. Without it the game fails to start.

**Do I need it on the server?**
Yes. The mod registers blocks, items and a menu, so client and server must both have it.

**Can I pick my arrows back up?**
Normal arrows and redstone arrows stick in the ground and can be retrieved. TNT arrows are consumed by the explosion; pearl arrows are consumed once the teleport succeeds.

**Will it conflict with other mods?**
It only rewrites vanilla arrow impact logic (one Mixin) and does not touch other mods' content. If something does break, open an issue with `latest.log` and reproduction steps.

**Any plans for 1.20.x / 1.21.1?**
1.21.11 only for now. Other versions are neither tested nor scheduled.

## Building from source

```bash
./gradlew build        # gradlew.bat on Windows
```

Output lands in `build/libs/`: `update-ft-1.0.0.jar` (the one you install) and `-sources.jar`. Requires JDK 21.

Project layout, what each file does, the registration and crafting flow, and the release process all live in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) (Chinese).

## Feedback

- Bugs / ideas: [Issues](https://github.com/ztqjntm/update-FT/issues)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
- Roadmap: [TODO.md](TODO.md)
- Author: [Bilibili](https://space.bilibili.com/3546940667071250)

## License

[Mozilla Public License 2.0](LICENSE). Use, modify and redistribute freely; modified MPL files must stay open under the same license.

<details>
<summary>Technical details</summary>

| Field | Value |
| --- | --- |
| Mod ID | `update-ft` |
| Version | 1.0.0 |
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| Java | 21 |
| Package | `ztqjntm.update` |
| Environment | Client + server (`*`) |
| License | MPL-2.0 |

</details>
