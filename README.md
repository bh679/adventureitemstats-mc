# Adventure Item Stats

Adds **Gaussian variation** to the base stats of items rolled by Minecraft's
loot tables. Naturally-spawned swords, axes, and armor come out with slightly
different attack damage, attack speed, armor value, and toughness, so two
"Iron Swords" found in two different chests no longer feel identical.

- **Loaders:** Fabric / Forge / NeoForge — Minecraft 1.21.1
- **License:** PolyForm Shield 1.0.0 (source-available)
- **Status:** v0.1.0 — scaffold (loot hook wired up; variation logic lands in v0.2)

Sister mod to [Adventure Item Names](https://github.com/bh679/adventureitemnames-mc) —
made for [Dungeon Train](https://brennanhatton.itch.io/dungeontrain).

## What it does

Every time a vanilla loot table rolls a weapon or armor piece, the mod nudges
its stats by a Gaussian random multiplier so most rolls land near vanilla and
a rare few roll far above or below. Stats varied (planned for v0.2):

- **Attack damage** — on swords, axes, tridents, and any item with a base
  attack-damage attribute modifier.
- **Attack speed** — paired with damage so a faster swing trades for less
  damage per hit and vice versa.
- **Armor + toughness** — on helmets, chestplates, leggings, boots.

Items rolled outside loot tables — crafted, given via `/give`, traded with
villagers — stay vanilla. The mod hooks the same vanilla `LootTable`
codepath that Adventure Item Names uses, so the two mods compose cleanly:
a single rolled sword can have both a generated name **and** rolled stats.

## Install

Pick the jar that matches your loader:

| Loader | Required | Download |
|--------|----------|----------|
| Fabric | [Fabric Loader](https://fabricmc.net/) 0.16+ and [Fabric API](https://modrinth.com/mod/fabric-api) | `adventureitemstats-fabric-0.1.0.jar` |
| Forge | [Forge](https://files.minecraftforge.net/) 1.21.1-52.1.x | `adventureitemstats-forge-0.1.0.jar` |
| NeoForge | [NeoForge](https://neoforged.net/) 21.1.228+ | `adventureitemstats-neoforge-0.1.0.jar` |

Drop the jar into your `mods/` folder and launch the game. No config required.

## Java API for other mods

If you have your own custom loot system and want rolled stats there too,
depend on this mod and call:

```java
import games.brennan.adventureitemstats.api.StatsModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

void onMyCustomLootRoll(ItemStack stack, RandomSource rng) {
    StatsModifier.applyStats(stack, rng);
    // stack now has rolled attribute modifiers if it's a weapon or armor
}
```

`applyStats` mutates the stack — it writes the rolled values to the stack's
`ATTRIBUTE_MODIFIERS` data component when the item has a relevant base stat,
and is a no-op otherwise. Pass a seeded `RandomSource` if you need
deterministic rolls per position/seed.

## Roadmap

- **v0.1:** Scaffold — loot mixin wired up, `StatsModifier.applyStats` is a no-op.
- **v0.2:** Gaussian variation on attack damage, attack speed, armor, toughness.
- **v0.3:** Per-stat distribution parameters loadable from a datapack.
- **v0.x:** In-game config screen for distribution σ.

## Credits

Concept from [Dungeon Train](https://brennanhatton.itch.io/dungeontrain) by
Brennan Hatton (2018). Sister-mod scaffolding adapted from
[Adventure Item Names](https://github.com/bh679/adventureitemnames-mc).
