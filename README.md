# MaxLevels

Set the maximum level of any enchantment on a Paper server.

MaxLevels changes the max level in the server's enchantment registry at startup. Everything in the game that reads it follows: the anvil, `/enchant`, enchanting tables, villager trades, loot, and the client's own preview. Enchantments added by datapacks work the same way.

## Install

1. Put `MaxLevels-<version>.jar` in `plugins/`.
2. Start the server once to create `plugins/MaxLevels/config.yml`.
3. Edit it and restart.

Requires Paper, Folia, or Purpur 1.21 to 26.3.

## Config

```yaml
sharpness: 10
efficiency: 10
mypack:lifesteal: 5
```

Each line is an enchantment key and its max level, from 1 to 255. Keys without a namespace mean `minecraft:`. Enchantments you don't list stay as they are. Changes need a restart.

On startup the console shows what was applied:

```
[MaxLevels] Applied sharpness 5 -> 10, efficiency 5 -> 10, mypack:lifesteal 1 -> 5
```

Invalid lines and unknown keys are reported and skipped. They never stop the server.

## Anvil cost

High levels make anvil work expensive. A Thorns X book alone costs 40, which vanilla refuses as "Too Expensive!". To allow it, add:

```yaml
anvil-max-cost: 100
```

This is the highest cost the anvil accepts, from 1 to 32766. Vanilla stops at 39. Players still pay the full cost in levels. The client always labels costs of 40 or more as "Too Expensive!" in red instead of showing the number, but the result can be taken.

## Levels above X

The game only has names for levels I to X, so level 11 shows as `enchantment.level.11`. Add `MaxLevels-ResourcePack-<version>.zip` from the release as your server resource pack, or merge its `en_us.json` into yours. It covers every client language.

## Good to know

- **Enchanting tables** still use vanilla costs, so they rarely go past level 4 or 5. Higher levels come from the anvil.
- **Loot and librarians** pick levels evenly up to the new max. Sharpness 100 means chest books average around 50.
- **Lowering a max** leaves existing items alone, but combining one in an anvil with the same enchantment caps it at the new max.
- **Anvil "Too Expensive!"** still applies at cost 40 unless you set [`anvil-max-cost`](#anvil-cost).
- **Some effects stop growing** past a level, and a few break:

| Enchantment | Past this level |
|---|---|
| Lure | 6+ stops fish from biting |
| Piercing | 128+ disables piercing |
| Luck of the Sea | Very high levels leave only treasure |
| Multishot | Each extra arrow costs durability |
| Depth Strider | No gain past 3 |
| Quick Charge | No gain past 5 |
| Thorns | Always triggers at 7 |
| Breach | Ignores all armor at 6 |
| Protection types | Capped at 80% reduction across all armor |
| Efficiency | No gain past 32 |

## Build

```
./gradlew build
scripts/boot-test.sh 26.3 build/libs/MaxLevels-*.jar
```

The boot test starts a real Paper server of the given version and checks the plugin end to end. It needs `java`, `curl`, and `jq`.

## License

MIT
