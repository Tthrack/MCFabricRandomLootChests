# Random Loot Chests (Fabric)

Server-side Fabric mod that supports Minecraft **1.21.1** and checks registered loot chests every 2 hours.

## Behavior

- Watches for OP-created chest placements with custom names:
  - `Loot`
  - `Loot +1`
  - `Loot +2`
- Multiple chests with the same name are supported.
- Chests with those names placed by non-OP players are ignored.
- Every 2 hours, if a tracked chest is empty, it is refilled from its matching loot table with 3 generated items.

## Loot tables

- `data/randomlootchests/loot_tables/chests/loot.json`
- `data/randomlootchests/loot_tables/chests/loot_plus_1.json`
- `data/randomlootchests/loot_tables/chests/loot_plus_2.json`

You can edit these to customize rewards.
