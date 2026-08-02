# GGWave

A store-purchase celebration plugin for Paper Minecraft servers. When an admin (or store webhook integration) triggers a "GG Wave" for a player, the server is announced to with a rendered player head and can congratulate them by typing "gg" in chat to trigger fireworks, sounds, and rewards.

**Author:** ISekai

## Features
- `/ggwave start <player> <purchase>` announces a celebration for a player, rendering their skin's face as colored block characters directly in chat
- Players type `gg` in chat during an active wave to participate; each "GG" message cycles through a configurable rainbow color loop
- First-time participants automatically receive configurable reward commands (run as console)
- Configurable player limit on how many participants can be rewarded per wave
- Animated firework-style particle effects (rings, spirals, and bursts) around the celebrated player for a configurable duration
- Title/subtitle display shown to the celebrated player
- Full MiniMessage and `&#RRGGBB` hex color support in all messages
- Optional PlaceholderAPI expansion (`%ggwave_active%`, `%ggwave_target%`, `%ggwave_purchase%`, `%ggwave_participants%`, `%ggwave_total_ggs%`, `%ggwave_participated%`)
- Optional LuckPerms integration to prefix chat "GG" messages with the player's rank prefix
- Admin commands to force-stop an active wave or reload configuration/messages live

## Commands
| Command | Description |
|---|---|
| `/ggwave start <player> <purchase>` | Start a GG wave celebration for a player |
| `/ggwave stop` | Force stop the active GG wave |
| `/ggwave reload` | Reload configuration and messages |

Alias: `/gg-wave`

## Permissions
| Permission | Default | Description |
|---|---|---|
| `ggwave.admin` | op | Access to all GGWave commands |
| `ggwave.participate` | true | Can participate in GG waves |

## Dependencies
- [Paper API](https://papermc.io/) 1.21.4-R0.1-SNAPSHOT (compile-only)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 2.11.6 (optional, soft dependency)
- [LuckPerms](https://luckperms.net/) (optional, soft dependency)
- Java 21

## Installation
1. Download the jar from the Releases page of this repository.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload the server.

## Building from source
```bash
./gradlew build
```
Compiled jar lands in `build/libs/` (Gradle) or `target/` (Maven).

## License
See [LICENSE](LICENSE). All rights reserved — see terms above.
