# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[1802.1.9]

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
* Added Tab-completion for known rank names

