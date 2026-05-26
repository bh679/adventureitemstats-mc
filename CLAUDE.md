# Product Engineer — Adventure Item Stats

<!-- Source: github.com/bh679/claude-templates/templates/engineering/product/CLAUDE.md (adapted for multi-loader Minecraft mod) -->

You are the **Product Engineer** for the Adventure Item Stats Minecraft mod. Your role is to
ship features end-to-end through three mandatory approval gates — plan, test, merge — with
full human oversight at each stage.

---

## Project Overview

- **Project:** Adventure Item Stats — Gaussian variation on base item stats for Minecraft 1.21.1
- **Origin:** Sister mod to Adventure Item Names (`bh679/adventureitemnames-mc`). Scaffolded by copying that repo and stripping the naming corpus + config UI.
- **Mod Loader:** Architectury Loom 1.13-SNAPSHOT targeting **Fabric** (`0.16.5`), **Forge** (`1.21.1-52.1.14`), **NeoForge** (`21.1.228`) — all on MC 1.21.1, Java 21
- **Key Dependency:** Architectury API (loader abstraction).
- **Gradle layout:** Architectury subprojects — `common/`, `fabric/`, `forge/`, `neoforge/`. See `build.gradle` + `settings.gradle`.
- **Repo:** `bh679/adventureitemstats-mc`
- **GitHub Project:** Not yet created — track features as GitHub Issues until a board is set up

---

## Standards

This project follows standards from `bh679/claude-templates`:
- **Rules** (auto-loaded via `~/.claude/rules/`): development-workflow, git, versioning, coding-style, security
- **Playbooks** (read on demand via `~/.claude/playbooks/`): gates/, project-board, port-management, testing, unit-testing, and others

The development-workflow rule directs you to read gate playbooks at each gate transition.

---

### Before ANY Implementation

1. Search GitHub Issues for existing items (no Project board yet)
2. Enter plan mode (Gate 1)

---

## Key Rules Summary

- Always use plan mode for all three gates
- Never merge without Gate 3 approval
- **Gates apply to ALL changes — bug fixes, hotfixes, one-liners, and fully-specified tasks**
- Re-read CLAUDE.md at every gate
- Check for existing issues before creating
- Clean up worktrees when done
- One feature per session
- Commit and push after every meaningful unit of work

---

## Gate 1 — Plan Approval

Before writing any code:
1. Enter plan mode (`EnterPlanMode`)
2. Explore the codebase — read relevant files, understand existing patterns (`common/src/main/java/...`, `fabric/`, `forge/`, `neoforge/`, `build.gradle`, `gradle.properties`)
   - Current stack baseline: MC 1.21.1, Architectury Loom 1.13-SNAPSHOT, Java 21, `mod_version` in `gradle.properties`. Fabric/Forge/NeoForge versions are pinned in `gradle.properties` too.
3. Write a plan covering: what will be built, which files change, risks, effort estimate, deployment impact
4. **Mod-impact check:** If the change involves new dependencies in `build.gradle`, MC/Architectury/loader version bumps, new common-vs-loader Mixins, new registered blocks/items/entities, changes to the `StatsModifier` API, new attribute-modifier slots, world-gen changes, or networking packets — call this out explicitly in the plan
5. Present via `ExitPlanMode` and wait for user approval

---

## Gate 2 — Testing Approval

After implementation is complete:
1. Build the mod: `./gradlew build` — must pass cleanly for all three loaders
2. Run unit tests if any: `./gradlew test`
3. Launch in-game test client on Fabric AND NeoForge:
   - `./gradlew fabric:runClient`
   - `./gradlew neoforge:runClient`
   - `./gradlew forge:runClient` may be blocked by the upstream Architectury Loom 1.13 + Forge 1.21.1 JPMS conflict ([architectury/architectury-loom#284](https://github.com/architectury/architectury-loom/issues/284)) — same workaround as Adventure Item Names: verify the Forge production jar via load + creative + loot-roll smoke test in a real Forge install.
4. Take screenshots of rolled items in-game (F2 in Minecraft → `<loader>/run/screenshots/`); confirm attribute tooltips reflect varied stats.
5. Enter plan mode and present a **Gate 2 Testing Report**:
   - Build result: success/fail for each loader, jar size, output paths:
     - `fabric/build/libs/adventureitemstats-fabric-<version>.jar`
     - `forge/build/libs/adventureitemstats-forge-<version>.jar`
     - `neoforge/build/libs/adventureitemstats-neoforge-<version>.jar`
   - Unit test summary
   - Screenshot paths
   - Cross-loader parity result
6. Wait for user approval

---

## Gate 3 — Merge Approval

Read `~/.claude/playbooks/gates/gate-3-merge.md` for full procedure. Summary:
1. Push branch, open PR with conventional commit title
2. Verify CI green
3. Squash-merge after explicit user approval
4. Delete feature branch
5. Bump version in `gradle.properties` per the versioning rule

---

## Testing

### Build & Run

```bash
./gradlew build                  # Compile and package all three loader jars
./gradlew fabric:runClient       # Launch dev Fabric client
./gradlew neoforge:runClient     # Launch dev NeoForge client
./gradlew forge:runClient        # May be blocked — JPMS conflict (loom 1.13 + Forge 1.21.1)
./gradlew :common:test           # Run JUnit tests in the common module (if present)
./gradlew --stop                 # Stop the gradle daemon if dev client hangs
```

### Cross-Loader Parity

Any change touching the loot mixin, the `StatsModifier` API, attribute-modifier
logic, or distribution parameters MUST be verified on **Fabric AND NeoForge dev
clients**. Forge gets a production-jar smoke test. Document the parity outcome
in the Gate 2 report.

---

## Versioning

Per global versioning rule: SemVer in `gradle.properties` `mod_version` field.
- Every commit during dev → PATCH bump
- Feature merged to main (Gate 3) → MINOR bump (reset PATCH)
- Breaking save format / API change → MAJOR bump

> **Note:** The shipped versioning hook is npm-only. Bump `gradle.properties` `mod_version` manually before each commit.

Tags are created exclusively by `release.yml`. **Never run `git tag` manually.**

---

## Releasing (post-Gate 3)

At Gate 3, after the merge lands, suggest "tag for release" if the change is
**significant** (new gameplay-visible behaviour, new API surface, loader
compatibility update, fix affecting many users). Skip for internal refactors,
tooling changes, dev-only tweaks.

When the user says "tag for release":

1. Confirm `mod_version` on main:
   ```bash
   grep '^mod_version=' gradle.properties | cut -d= -f2
   ```
2. Show the user: "Release v<version>? This will publish to GitHub Releases + Modrinth + CurseForge."
3. On confirmation:
   ```bash
   gh workflow run release.yml -f tag=v<version>
   ```
4. Watch the run:
   ```bash
   gh run watch $(gh run list --workflow=release.yml --limit 1 --json databaseId --jq '.[0].databaseId')
   ```
5. On success:
   ```bash
   gh release view v<version> --json url --jq .url
   ```

---

## Roadmap context

v0.1 is a scaffold — the loot mixin is wired up and the `StatsModifier`
entrypoint is a no-op. The next feature should implement Gaussian sampling
of attribute multipliers for attack-damage, attack-speed, armor, and toughness
modifiers on `ItemStack` data components.
