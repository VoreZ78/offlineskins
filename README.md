# OfflineSkins-Reloaded-1.21.6-1.21.8

OfflineSkins Reloaded for Minecraft 1.21.6-1.21.8.

A client-side Fabric mod that allows player skins and capes to be loaded from the local cache, making them available even when Mojang services are unavailable or when playing in offline mode.

OfflineSkins Reloaded is a heavily reworked and optimized version of the original OfflineSkins project, with a redesigned core, improved networking, configuration system, server validation and additional client-side features.

## Features

- 🎨 Offline skin support
- 🦸 Offline cape support
- 📁 Local texture cache loading
- ⚡ Optimized networking and lightweight client-side operation
- 🧩 Fabric 1.21.6-1.21.8 support
- 🌍 17 languages supported
- ⚙️ YACL configuration menu
- 🔍 Server and URL validation
- 🖥️ Configuration button in the Title Screen
- ⏸️ Configuration button in the Pause Screen
- ⌨️ `U` keybind for quickly opening the configuration menu
- 💬 `/offlineskins menu` command
- ℹ️ `/offlineskins version` command
- 🖼️ HD skin and cape support
- 🚫 HD texture restrictions with configurable `allowHD` support
- ❗ Replacement textures for HD skins and capes when HD textures are not allowed
- 🔄 Improved and modernized skin loading architecture

## New Features

- 🗄️ Added FSCS technology or Fast Skin and Cape Selection
- 🔄 Added FSCRS technology or Fast Skin and Cape Recache System
- 🖼️ Added LSC-F technology or Legacy Skin and Cape Filter
- ℹ️ Added more tabs to the OfflineSkins Reloaded menu
- ⚡ Code cleanup and bug fixes
- ⚡ Improved optimization and network performance
- ❓ Added an FAQ menu for common questions

## Configuration

OfflineSkins Reloaded provides a YACL-based configuration menu.

The configuration can be opened through:

- The button in the Minecraft Title Screen
- The button in the Pause Screen
- The `U` key
- `/offlineskins menu`

## Legacy Filters

- Remaps old 64x32 skin textures to modern 64x64 without quality loss

- Remaps old 22x17 cape textures to modern 64x32 without quality loss

## HD Textures

OfflineSkins Reloaded supports high-resolution skins and capes.

HD textures can be enabled through the `allowHD` setting for supported servers.

When HD textures are not allowed for players on a server, the mod can replace oversized skins and capes with special fallback textures:

- `SkinHDNotAllowed.png`
- `CapeHDNotAllowed.png`

This helps optimize network traffic from players using high-resolution skins or capes when HD textures are not permitted.

The local cached texture system can still contain textures of arbitrary supported resolutions.

## Cached Textures

The mod uses the local cache directory:

## Custom Server Skin and Cape Providers

The mod also supports custom skin and cape providers, allowing skins and capes from external servers to be loaded into your game.

### Smart Internet Check

**Smart Internet Check** automatically detects when your internet connection is unavailable and prevents unnecessary network requests.

Your local skins and capes from `cachedImages` are never affected by this check and continue to work normally. Cached skins and capes can also be enabled or disabled independently in the configuration.

## ⚠️ Content Warning

OfflineSkins Reloaded may load skins and capes from third-party services.
Third-party services may contain user-created content that can be provocative, offensive, disturbing or otherwise inappropriate. This content is not controlled, moderated or endorsed by the OfflineSkins Reloaded project.
Use third-party skin and cape sources at your own discretion.
```text
cachedimages/
├── skins/
└── capes/
Skins

Place player skin textures inside:

cachedimages/skins/

Example:

cachedimages/skins/Steve.png
Capes

Place cape textures inside:

cachedimages/capes/

Example:

cachedimages/capes/Steve.png

The filename must match the player's username.

Commands
Open configuration
/offlineskins menu

Opens the OfflineSkins Reloaded configuration menu.

Show version
/offlineskins version

Displays the currently installed OfflineSkins Reloaded version.

Keybinds

By default:

U

opens the OfflineSkins Reloaded configuration menu.

The keybind can be changed through Minecraft's Controls menu.

Server and URL Validation

OfflineSkins Reloaded includes validation for custom skin and cape sources.

This helps prevent invalid or unsupported image data from being loaded as player skins or capes.

Languages

OfflineSkins Reloaded supports 17 languages.

The localization files are included with the mod and cover the configuration menu and its options.

Credits
Original Project

Author: zlainsama

Original Repository:
https://github.com/zlainsama/OfflineSkins

Minecraft Fabric Port / OfflineSkins Reloaded

Author: VoreZ

Repository:
https://github.com/VoreZ78/offlineskins

OfflineSkins Reloaded contains substantial changes and improvements over the original project, including a reworked core, optimized networking, a new configuration system, server validation, client UI integration, HD texture handling and additional commands and keybindings.

License

This project is licensed under the MIT License.

See the LICENSE file for details.