## QuestShop

QuestShop is a lightweight Minecraft (NeoForge) mod that provides an in-game shop and reward progression system.
It is designed for modpack makers to support controlled item access, team play, and quest progression.

Inspired by [FTB Money](https://www.curseforge.com/minecraft/mc-mods/ftb-money-forge).

👩‍💻 Author: [Holysweet](https://www.curseforge.com/members/holysweet/projects)  
🧑‍💻 Developed by: [AquariusSidhe](https://www.curseforge.com/members/aquariussidhe/projects) (GitHub: alexanderheuts)  
📜 License: MIT

### Purpose

* Provide a fully customizable shop where players can spend earned coins. 
* Coins can be earned with QuestRewards, or given with commands.
* Support progression gating — Shop Categories can be (un)locked with commands.

### Integrations

QuestShop works on its own, but offers optional integration with:

* [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge) – for shared team progression.
* [FTB Quests](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge) – for quest-based rewards or unlocks. 

_No hard dependencies: packs can include QuestShop standalone or alongside these mods._

---

### Commands

All in-game commands are attached under: ```/hqs```

To open the shop use: ```/hqs shop```

---

### Shop Configuration

All shop content is defined via datapacks.

#### Shop category
```
data/yourpack/questshop/shop_categories/building.json
```

```
{ "display": "Building", "unlocked_by_default": true, "order": 30 }
```

* The filename determines the category ID. 
* One file per category.
* display - name shown in game.
* unlocked_by_default - is the category, and its items, unlocked by default
* order - integer for default ordering, compared to other categories, higher is lower on the list

#### Shop entry
```
data/yourpack/questshop/shop_entries/building_basic.json
```

```
[
  { 
    "item": "minecraft:cobblestone",
    "amount": 64, 
    "cost": 2,  
    "category": "building" 
  },
  { 
    "item": "minecraft:oak_planks",
    "amount": 64, 
    "cost": 2,  
    "category": "building" 
  }
]
```
* Filenames are arbitrary. Used for your own grouping.
* item - minecraft item ID to sell.
* amount - the amount you get when purchased. 
* price - cost in the built-in currency.
* category - category ID the item belongs to.

Folder structure (datapack):
```
(your_datapack)/
  pack.mcmeta
  data/
    yourpack/
      questshop/
        shop_categories/
          building.json
        shop_entries/
          building_basic.json
```

See [repository](https://github.com/alexanderheuts/quest-shop/tree/main/src/main/resources/datapacks/questshop_examples) for example datapack.

