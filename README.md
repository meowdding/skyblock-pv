<h1 align="center">
  SkyBlock Profile Viewer
</h1>

<div align="center">

[![Discord](https://img.shields.io/discord/1296157888343179264?color=8c03fc&label=Discord&logo=discord&logoColor=white)](https://discord.gg/FsRc2GUwZR)
<!--[![Modrinth](https://img.shields.io/modrinth/dt/skyblock-profile-viewer?color=%23007EA7&label=Modrinth Downloads&style=for-the-badge)](https://modrinth.com/mod/skyblock-profile-viewer)-->

</div>

A Profile Viewer for Hypixel SkyBlock, developed with love and passion.

The Profile Viewer can be opened with ``/pv`` for your own Profile, or ``/pv <username>`` for someone else.
<br/>You can switch between the tabs using the buttons on the right side of the UI.
<br/>If a tab has multiple categories, you can switch between them using the buttons on the top of the UI.

> [!CAUTION]
> If you get a chat message with "Something went wrong :3", it means our temporary API Key has expired.
> You can't update the key yourself, you need to join the server above and ping J10a1n15 or ThatGravyBoat to update it.
> They key is now hosted separately in the proxy in the attempt of the admins to actually finally give us a permanent key.

> [!WARNING]
> The mod is still in development, design is subject to change.
> Visit the [TODO.md](./TODO.md) for a list of features that are planned to be added.

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
All fishing related information, so Essence Upgrades, Trophy Fish, Gear, Stats, ... in one beautiful tab.

### 🔍 And many more tabs...

...we just didn't put them in the ReadMe. Look at them when pv'ing yourself or someone else!

### Mod Compatibility

- Using [SkyBlockMod](https://github.com/kevinthegreat1/SkyblockMod-Fabric) is unsupported, they add own command shortcuts, which you can
  disable, but that disables all the shortcuts. They shorten ``/party leave`` to ``pv``, which is the same as our command.
- [SkyBlocker](https://github.com/SkyblockerMod/Skyblocker) add their own Profile Viewer. We override their ``/pv`` command with ours, if you wish to use
  theirs, you can use ``/skyblocker pv``. We don't have an option to disable this behavior since because you're already this mod, its unlikely you want to
  prefer SkyBlocker's Profile Viewer.
