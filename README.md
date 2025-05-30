<h1 align="center">
  SkyBlock Profile Viewer
</h1>

<div align="center">

[![Discord](https://img.shields.io/discord/1296157888343179264?color=8c03fc&label=Discord&logo=discord&logoColor=white)](https://discord.gg/FsRc2GUwZR)
[![Modrinth](https://img.shields.io/modrinth/dt/8yqXwFLl?style=flat&logo=modrinth)](https://modrinth.com/mod/skyblock-profile-viewer)

</div>

A Profile Viewer for Hypixel SkyBlock, developed with love and passion.

The Profile Viewer can be opened with ``/pv`` for your own Profile, or ``/pv <username>`` for someone else.
<br/>You can switch between the tabs using the buttons on the right side of the UI.
<br/>If a tab has multiple categories, you can switch between them using the buttons on the top of the UI.

> [!WARNING]
> The mod is still in development, design is subject to change.
> Screenshots may be outdated.

### 🏠 Home Tab

<img src="./.github/images/home.png" width="720" alt="" title="Home">
Designed to be simple and to show off the main aspects of the player's profile.
<br/>Designed with a purpose to be screenshot-able and shareable.

### 📦 Inventory Tab

<img src="./.github/images/backpack.png" width="720" alt="" title="Backpacks">
Switch between EnderChest, Backpack, ... pages using the custom build Carousel or the buttons up top.

### 📚 Collections Tab

<img src="./.github/images/collection.png" width="720" alt="" title="Collections">

### 🎣 Fishing Tab

<img src="./.github/images/fishing.png" width="720" alt="" title="Fishing">
All fishing related information, so Essence Upgrades, Trophy Fish, Gear, Stats, ... in one tab.

### 🔍 And many more tabs...

...we just didn't put them in the ReadMe. Look at them when pv'ing yourself or someone else!

<details>
<summary>All Tabs</summary>

as of 2025-04-16

- Home
- Combat
    - Dungeons
    - Bestiary
  - Crimson Isle
    - Mob Kills
- Inventory
    - Main Inventory
    - Ender Chest
    - Backpacks
    - Wardrobe
    - Accessory
    - Sacks
  - Personal Vault, Potion Bag, Fishing Bag, Quiver, Candy Bag, etc.
- Collections
    - With Minions
- Mining
    - Main Mining
    - Mining Gear
    - HotM
    - Glacite
- Fishing
- Pets
- Farming
    - Main Farming
    - Visitors
    - Crops
    - Composter
- Museum
    - Weapons
    - Armor
- Chocolate Factory
- Rift
    - Main Rift
    - Inventory
    - Ender Chest

</details>

### Mod Compatibility

#### SkyBlockMod

- **Issue**: [SkyBlockMod](https://github.com/kevinthegreat1/SkyblockMod-Fabric) abbreviates `/party leave` to `pv`, which conflicts with our command.
- **Resolution**: We remove the `/pv` shortcut so that our command is used.
- **Note**: We do not provide an option to disable this behaviour, as using our mod implies you want the pv shortcut for profile viewer.

#### SkyBlocker

- **Issue**: [SkyBlocker](https://github.com/SkyblockerMod/Skyblocker) includes its own Profile Viewer.
- **Resolution**: We override their `/pv` command with ours. If you prefer SkyBlocker's version, use `/skyblocker pv` instead.
- **Note**: We do not provide an option to disable this override, as using our mod implies a preference for our Profile Viewer.  
