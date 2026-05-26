# Publishing — Modrinth & CurseForge

Step-by-step setup for the first release. Reuse this file for future releases
to keep platform copy in sync.

The actual file upload is automated by [`.github/workflows/release.yml`](../.github/workflows/release.yml).
What's manual is the one-time project creation on each platform and the
GitHub repo configuration that lets the workflow authenticate.

---

## 1. Create the Modrinth project

1. Sign in at https://modrinth.com and go to https://modrinth.com/dashboard/projects → **Create a project**.
2. Fill out the form with the values below.
3. After saving, copy the **Project ID** from Settings → General.

### Modrinth fields — copy-paste

| Field | Value |
|---|---|
| **Name** | `Adventure Item Stats` |
| **Slug** (URL) | `adventure-item-stats` |
| **Summary** (one-liner, plain text) | `Gaussian variation on base stats of naturally-spawned items — every found sword and armor piece rolls a little different.` |
| **Project type** | Mod |
| **Categories** | `equipment`, `adventure`, `game-mechanics` |
| **Client side** | Required |
| **Server side** | Required |
| **License** | Custom → `PolyForm Shield 1.0.0`, URL `https://polyformproject.org/licenses/shield/1.0.0/` |
| **Source code URL** | `https://github.com/bh679/adventureitemstats-mc` |
| **Issue tracker URL** | `https://github.com/bh679/adventureitemstats-mc/issues` |
| **Wiki page URL** | _(leave blank for now)_ |

### Modrinth body (markdown — paste into the **Description** field)

```markdown
# Adventure Item Stats

**Every found sword feels different.**

Adventure Item Stats adds Gaussian variation to the base stats of items rolled by Minecraft's loot tables. Two Iron Swords found in two different dungeon chests no longer feel identical — each rolls a slightly different attack damage, attack speed, armor value, or toughness. Most rolls land near vanilla; a rare few roll far above or below.

## What gets varied

- **Attack damage** and **attack speed** on weapons (swords, axes, tridents, …)
- **Armor value** and **toughness** on helmets, chestplates, leggings, boots

Items you craft or `/give` yourself keep vanilla stats. Only naturally-spawned loot is touched.

## Combines with Adventure Item Names

This is the sister mod to [Adventure Item Names](https://modrinth.com/mod/adventure-item-names). Install both and a single rolled item gets **both** a procedurally-generated name **and** rolled stats — so the chest you just opened might contain a *Whispering Diamond Blade of Iron* that swings 18% faster than a normal Diamond Sword.

## Loaders

- **Fabric** — requires Fabric API
- **Forge**
- **NeoForge**

Minecraft 1.21.1, Java 21.

## License

Source-available under [PolyForm Shield 1.0.0](https://polyformproject.org/licenses/shield/1.0.0/).

Made for [Dungeon Train](https://brennanhatton.itch.io/dungeontrain).
```

---

## 2. Create the CurseForge project

1. Sign in at https://www.curseforge.com and go to **Author Dashboard** → **Create Project**.
2. Pick **Minecraft → Mods** as the project type. Fill out the form below.
3. After approval (CurseForge moderates new projects — usually < 24h), copy the **Project ID** from the right sidebar of the project page.

### CurseForge fields — copy-paste

| Field | Value |
|---|---|
| **Project Name** | `Adventure Item Stats` |
| **Project URL** (slug) | `adventure-item-stats` |
| **Summary** (max 255 chars) | `Gaussian variation on base stats of naturally-spawned items — every found sword and armor piece rolls a little different. Sister mod to Adventure Item Names.` |
| **Categories** | `Adventure and RPG`, `Equipment`, `Server Utility` _(pick the closest 2–3 — CurseForge cap is 4)_ |
| **Mod Loaders** | Fabric, Forge, NeoForge |
| **Supported game versions** | 1.21.1 |
| **License** | Custom (link to `https://github.com/bh679/adventureitemstats-mc/blob/main/LICENSE`) |

### CurseForge description (rich editor — paste the markdown below, then click "Source" → paste, or just paste into the rich editor and it converts)

_Use the same body as Modrinth above._ The CurseForge rich editor handles markdown paste cleanly. If you prefer BBCode, paste this instead:

```bbcode
[h1]Adventure Item Stats[/h1]

[b]Every found sword feels different.[/b]

Adventure Item Stats adds Gaussian variation to the base stats of items rolled by Minecraft's loot tables. Two Iron Swords found in two different dungeon chests no longer feel identical — each rolls a slightly different attack damage, attack speed, armor value, or toughness. Most rolls land near vanilla; a rare few roll far above or below.

[h2]What gets varied[/h2]

[list]
[*][b]Attack damage[/b] and [b]attack speed[/b] on weapons (swords, axes, tridents, …)
[*][b]Armor value[/b] and [b]toughness[/b] on helmets, chestplates, leggings, boots
[/list]

Items you craft or [b]/give[/b] yourself keep vanilla stats. Only naturally-spawned loot is touched.

[h2]Combines with Adventure Item Names[/h2]

This is the sister mod to [url=https://www.curseforge.com/minecraft/mc-mods/adventure-item-names]Adventure Item Names[/url]. Install both and a single rolled item gets [b]both[/b] a procedurally-generated name [b]and[/b] rolled stats.

[h2]Loaders[/h2]

[list]
[*][b]Fabric[/b] — requires Fabric API
[*][b]Forge[/b]
[*][b]NeoForge[/b]
[/list]

Minecraft 1.21.1, Java 21.

[h2]License[/h2]

Source-available under [url=https://polyformproject.org/licenses/shield/1.0.0/]PolyForm Shield 1.0.0[/url].

Made for [url=https://brennanhatton.itch.io/dungeontrain]Dungeon Train[/url].
```

---

## 3. Wire the platforms into GitHub Actions

[`release.yml`](../.github/workflows/release.yml) reads:

| GitHub setting | Source |
|---|---|
| **Variable** `MODRINTH_PROJECT_ID` | Modrinth project Settings → General → Project ID |
| **Variable** `CURSEFORGE_PROJECT_ID` | CurseForge project page → right sidebar → Project ID |
| **Secret** `MODRINTH_TOKEN` | https://modrinth.com/settings/pats → create PAT with scopes `Create version`, `Read user details` |
| **Secret** `CURSEFORGE_TOKEN` | https://legacy.curseforge.com/account/api-tokens → generate |

Set them with `gh`:

```bash
# After creating the Modrinth project:
gh variable set MODRINTH_PROJECT_ID    --body "<modrinth-project-id>"   --repo bh679/adventureitemstats-mc

# After creating the CurseForge project (and waiting for it to be approved):
gh variable set CURSEFORGE_PROJECT_ID  --body "<curseforge-project-id>" --repo bh679/adventureitemstats-mc

# Secrets — gh prompts for the value:
gh secret set MODRINTH_TOKEN    --repo bh679/adventureitemstats-mc
gh secret set CURSEFORGE_TOKEN  --repo bh679/adventureitemstats-mc
```

Until both the variable and the secret are set for a platform, the workflow
prints a `::warning::` and skips that platform — it does **not** fail. So
you can wire Modrinth first, ship a release, and add CurseForge later.

---

## 4. First release

Once GitHub Actions has at least Modrinth wired up:

```bash
# Confirm gradle.properties mod_version matches what you want to ship:
grep '^mod_version=' gradle.properties

# Trigger the release. The tag is created by the workflow, not by you:
gh workflow run release.yml -f tag=v0.1.0

# Watch it:
gh run watch $(gh run list --workflow=release.yml --limit 1 --json databaseId --jq '.[0].databaseId')

# When it finishes:
gh release view v0.1.0 --json url --jq .url
```

---

## Future-release checklist

- [ ] Bump `gradle.properties` `mod_version` (MINOR for new gameplay, PATCH for bugfixes).
- [ ] Update the **Description** body on Modrinth + CurseForge if the gameplay scope changed.
- [ ] If a new MC version is supported, update `minecraft_version` in `gradle.properties` **and** the `game-versions: 1.21.1` lines in `release.yml`.
- [ ] Run `gh workflow run release.yml -f tag=v<version>`.
