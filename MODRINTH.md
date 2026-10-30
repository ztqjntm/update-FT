<!--
Modrinth 上传用：
  · “Summary” 一节填进表单的 Summary 字段（≤256 字符）
  · “Description” 以下整段粘进 Description（支持 Markdown）
  · 建议分类：Adventure / Equipment / Game Mechanics；环境选 Client and Server；Loader 选 Fabric
-->

## Summary（表单字段，≤256 字符）

Vanilla's fletching table finally does something: a real GUI, nine arrow types (explosive, lightning, teleport, redstone and more), and a three-slot crafting system built around tip, shaft and fletching.

---

# Description

# update-ft

Vanilla's fletching table has been a decoration since 1.14 — it only existed to employ a villager. **update-ft** gives it a real purpose: a proper crafting interface, nine new arrow types, and a crafting system built around how an arrow is actually made.

## A fletching table that works

Right-click a fletching table to open a custom interface with **three fixed ingredient slots** and an output slot:

| Slot | Position | Accepts |
| :--- | :--- | :--- |
| **Tip** | top-right | the arrowhead material |
| **Shaft** | middle | sticks only |
| **Fletching** | bottom-left | feathers only |

The slots are locked, so there is no guessing: wrong items simply won't go in, and shift-clicking drops each item straight into the slot it belongs to.

No block entity is involved. The grid is created when you open it, and **everything returns to your inventory when you close it** — exactly like a crafting table, not on the floor.

The panel also closes by itself if you walk more than 8 blocks away or the table gets broken.

## Nine arrows

Every arrow is made at the fletching table from **tip + stick + feather**:

| Arrow | Damage | What it does |
| :--- | :---: | :--- |
| Stone | ±0 | Plain and cheap. |
| Copper | ±0 | **Calls down lightning** where it lands during a thunderstorm — only when the impact can see the sky. The strike is credited to the shooter. |
| Iron | +1 | A solid all-rounder. |
| Gold | ±0 | Light, shiny, and about as useful as you'd expect. |
| Diamond | +2 | Reliable damage. |
| Netherite | +4 | The heaviest hitter in the set. |
| TNT | −1 | **Explodes on impact** (radius 4). Respects `mobGriefing`: turn it off and it only hurts entities. Entity damage is halved. |
| Redstone | −1 | **Fires a redstone pulse.** Buttons, levers, doors, fence gates, trapdoors and bells are triggered in a 2-block radius, while an invisible pulse block powers lamps, pistons, dust, dispensers and rails for 8 ticks. |
| Pearl | ±0 | **Teleports the shooter to the impact point**, at the cost of 3 fall damage — like an ender pearl, but you can throw it across a ravine. |

## Recipes

The tip decides the result; the stick and feather are always consumed one each:

| Tip | Result | Yield |
| :--- | :--- | :---: |
| Flint | Arrow | 4 |
| Cobblestone | Stone Arrow | 4 |
| Copper Ingot | Copper Arrow | 4 |
| Iron Ingot | Iron Arrow | 4 |
| Gold Ingot | Gold Arrow | 4 |
| Diamond | Diamond Arrow | 4 |
| Netherite Ingot | Netherite Arrow | 4 |
| TNT Block | TNT Arrow | 1 |
| Redstone Dust | Redstone Arrow | 4 |
| Ender Pearl | Pearl Arrow | 2 |

## Requirements

- **Minecraft 1.21.11**
- **Fabric Loader** 0.19.5 or newer
- **Fabric API** 0.141.6 or newer
- **Java 21**

The GUI is a custom menu, so the mod needs to be installed on **both client and server**.

## Notes

- Recipes are currently hardcoded; there is no recipe book or JEI/REI integration yet.
- The fletching table contents are intentionally not persistent — closing the screen returns everything to you.
- Crossbow, bow and dispenser all work with these arrows (they are part of `minecraft:arrows`).

## License

[Mozilla Public License 2.0](https://github.com/ztqjntm/update-FT/blob/main/LICENSE) · [Source on GitHub](https://github.com/ztqjntm/update-FT)
