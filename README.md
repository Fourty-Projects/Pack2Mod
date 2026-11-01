# Pack2Mod

Force embedded resource packs on every client, from the very first game start — with no action required from players and no way to disable them in game.

Minecraft **1.20.1** · Forge **47.4.x** · Java **17**

---

## What it does

Pack2Mod bakes resource packs **directly into the mod JAR** and registers them as
**required, top-priority, built-in** client resource packs:

- ✅ Active from the **first launch**, before the main menu even shows
- ✅ **Hidden** from the resource pack selection screen (by design)
- ✅ **Cannot be disabled or removed** by players in game
- ✅ **Top priority** over vanilla and other packs
- ✅ Supports **multiple packs** at once
- ✅ Nothing to configure, no config files, no commands

Typical uses: custom GUIs, modpack branding, translated assets, enforced visual
overrides, servers/modpacks that must look identical for every player.

## Requirements (for players)

- Minecraft **1.20.1**
- Minecraft Forge **47.4.x** (built against 47.4.18, works on the whole 47.4 line)
- Java **17**
- Install like any Forge mod: drop the `.jar` into your `mods/` folder

> The mod is **client-side only** (`displayTest = IGNORE_ALL_VERSION`):
> install it on the **client**. Servers don't need it at all (it does nothing
> there), and players can join any server with it installed.

## For modpack makers / distributors

Pack2Mod is a **template**: you fork it, drop your packs in, compile, and ship
your own JAR with the packs baked in.

### 1. Add your packs

Unzip each resource pack into its **own subfolder**:

```
src/main/resources/forcedpacks/<your_pack>/pack.mcmeta
src/main/resources/forcedpacks/<your_pack>/pack.png        (optional)
src/main/resources/forcedpacks/<your_pack>/assets/...
```

Example: `src/main/resources/forcedpacks/my_gui/assets/minecraft/textures/...`

Rules:

- One folder = one pack. Each folder **must** contain a valid `pack.mcmeta`.
- `pack_format` must be **15** (Minecraft 1.20.1). Packs made for other versions
  will be ignored or flagged incompatible by the game.
- Do **not** put `.zip` files in `forcedpacks/` — only unzipped folders are
  detected (a `.zip` inside the JAR cannot be scanned as a pack).
- You can ship as many packs as you want side by side.

### 2. Build

```powershell
# Windows (wrapper included, downloads Gradle automatically)
.\gradlew.bat build

# Linux / macOS
./gradlew build
```

Requires JDK 17. Output: `build/libs/pack2mod-<version>.jar`.

> Tip: test without building with `.\gradlew.bat runClient` (or `./gradlew runClient`).
> After **deleting or renaming** a pack in `forcedpacks/`, always build with
> `clean` (`.\gradlew.bat clean build`) — Gradle never deletes stale copied
> resources, otherwise deleted packs stay in the JAR and in game.

Pushing a tag `vX.Y.Z` triggers `.github/workflows/build.yml`, which builds
the jar and publishes a GitHub Release with it. Upload the released jar on
Modrinth/CurseForge as a 1.20.1 version (Forge 47.4.x).

### 3. Ship it

Distribute the built JAR (Modrinth, CurseForge, Discord, …). Players install it
in `mods/` — the packs come with it and apply automatically.

> Note: pack contents under `src/main/resources/forcedpacks/` are git-ignored
> by default (see `.gitignore`), so private packs are never pushed to a public
> repo by accident. The compiled JAR still contains them.

## How it works (technical)

- Each subfolder of `forcedpacks/` containing a `pack.mcmeta` is registered
  through Forge's `AddPackFindersEvent` (mod bus) as a client pack with
  `required = true`, `position = TOP`, `source = BUILT_IN`.
- Required packs are selected automatically by the game on **every** resource
  reload, starting with the very first one at boot — no player action, no
  extra reload needed.
- Because the packs live inside the mod JAR, there is nothing to edit, move
  or delete from the game folders.

## FAQ

**The pack doesn't show up in the resource pack list — is it broken?**
No, that's expected. Required built-in packs are hidden from that screen.
Check the visuals in game (GUI, textures, sounds) instead.

**The pack doesn't apply at all, what do I check?**
1. The game log must contain `[Pack2Mod] Forcing pack: builtin/pack2mod_<name>`.
   If it says `No forced pack found in forcedpacks/`, the `forcedpacks/` folder is empty or
   missing `pack.mcmeta` files.
2. The pack's `pack.mcmeta` must declare `pack_format: 15`.
3. You must do a **full game restart** after embedding a new pack (it is part
   of the JAR — `F3+T` alone is not enough for a newly added one).
4. Client-side only: a dedicated server will not apply visuals.

**Can players override the forced packs with their own?**
Forced packs sit at top priority, so their assets win over vanilla and over
regular packs below them.

**Which Forge versions are supported?**
Minecraft **1.20.1** with Forge **47.4.x** (built and tested against 47.4.18).
This mod targets 1.20.1 only.

**License?**
MIT — do whatever you want with it, including shipping it inside commercial
modpacks. Credit appreciated but not required.
