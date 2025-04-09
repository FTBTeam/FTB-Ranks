# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2101.1.3]

### Added
* New condition: `rank_applies`
  * This is very similar to the existing `rank_added`, but does not require the rank to be explicitly added to the player
  * It just requires that rank currently applies to the player, either implicitly or explicitly
  * These two conditions can be useful to create more complex conditions by combining them with the boolean conditions (and/or/not)

## [2100.1.2]

### Changed
* API change for RankManager#createRank
  * Added new RankManager#createRank(String, int, boolean) method, deprecated the existing RankManager#createRank(String, String, int) method
  * Note: minor API break here: both methods now throw a RankException if the rank already exists, rather than blindly overwrite it

## [2100.1.1]

### Changed
* Now loads a supplementary `config/ftbranks-pack.snbt` file alongside the default file
  * Intended to augment existing ranks from `ranks.snbt` with pack-specific settings, to be created by modpack makers, independent of the server admin settings.

## [2100.1.0]

### Changed
* Ported to Minecraft 1.21. Support for Fabric and NeoForge.
  * Forge support may be re-added if/when Architectury adds support for Forge

## [2006.1.0]

### Changed
* Ported to Minecraft 1.20.6. Support for Fabric and NeoForge.
  * Forge support may be re-added if/when Architectury adds support for Forge

## [2004.2.0]

### Changed
* Ported to MC 1.20.4: Forge, NeoForge and Fabric all supported

## [1902.1.16]

### Fixed
* Correctly report errors and stop when a syntax error in ranks.snbt or players.snbt prevents the file from loading
  * Don't tell the player it loaded OK then wipe the current runtime config...

## [1902.1.15]

### Added
* Added `/ftbranks node list <rank>` command to view the permissions nodes that are added to a given rank
* Some backend code improvements for Fabric, related to player display name processing
  * Now using a custom Fabric event in FTB Library for better inter-mod compatibility with upcoming Fabric version of FTB Essentials

## [1902.1.14]

### Fixed
* Backed out dynamic tab-completion functionality and use simple server-side suggestions instead
  * FTB Ranks is a server-only mod again and no longer needs to be installed on the client since that's unnecessarily disruptive

## [1902.1.13]

### Fixed
* Made rank-based chat text colouring (using `ftbranks.chat_text.*` nodes in `ranks.snbt`) work correctly on Forge and Fabric
* FTB Ranks is no longer incorrectly marked as a server-only mod
  * As of the previous release, it is also required on the client (to support Tab-completion for ranks in commands)

## [1902.1.12]

### Added
* Events are now fired for other mods to consume when various things happen:
    * Config reloaded with `/ftbranks reload`
    * A rank is created or deleted
    * Player is added to or removed from a rank
    * A permission node of a rank is modified
    * A rank's condition is modified
* Added `/ftbranks node <rank> <nodename> <value>` command to change a permission node's value
* Added `/ftbranks condition <rank> <condition>` command to change a rank's condition
    * `<condition>` is an SNBT serialized condition e.g. `op` or `{ type "dimension", "dimension": "minecraft:the_nether" }`
* Added `/ftbrank show_rank <rank>` to display details of a rank
* Added Tab-completion for know rank names

### Fixes
* Player name decorating in chat messages (controlled by the `ftbranks:name_format` node) now works again
