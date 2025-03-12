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

### 🔍 And many more tabs...

...we just didn't put them in the ReadMe. Look at them when pv'ing yourself or someone else!

### Mod Compatibility

- Using [SkyBlockMod](https://github.com/kevinthegreat1/SkyblockMod-Fabric) is unsupported, they add own command shortcuts, which you can
  disable, but that disables all the shortcuts. They shorten ``/party leave`` to ``pv``, which is the same as our command.
- [SkyBlocker](https://github.com/SkyblockerMod/Skyblocker) add their own Profile Viewer. We override their ``/pv`` command with ours, if you wish to use
  theirs, you can use ``/skyblocker pv``. We don't have an option to disable this behavior since because you're already this mod, its unlikely you want to
  prefer SkyBlocker's Profile Viewer. 

### Contributing

Since Hypixel is very awesome they keep rejecting our permanent API-Key requests, so we have to use their temporary daily keys.
<br/>(If any admins read this, please give us a perm key, please. Or at least state why you keep rejecting it literally doesn't show any reason)
<br/>If the gui keeps loading and a chat message "Something went wrong :3" appears, it's likely an expired key.
<br/>You can get a new key from the [Developer Dashboard](https://developer.hypixel.net/dashboard) and replace it in HypixelAPI.kt.
