# FBasics

*Supplies features most faction servers will require*

## Supports
* CraftBukkit & Spigot 1.7.9
* CraftBukkit & Spigot 1.7.10
* Spigot 1.7.10 / 1.8 Protocol Patch
* CraftBukkit & Spigot 1.8.0
* CraftBukkit & Spigot 1.8.3

## Features

* Patches many issues found on factions servers
  * Glitching through walls with horses, and boats
  * Promoting players via Buycraft and Enjin with a safety catch
  * Teleporting through walls with enderpearls or to the top of the nether
  * Duplicating crops by placing doors and other materials next to them
  * Duplicating ores with mcMMO
  * Duplicating consumables by blowing up inventory blocks
  * Any V-Clip or Phase hack through solid walls
* Supports Factions for checking territories with commands, wilderness and enderpearls
  * _Most mainstream Faction builds are supported:_
  * Factions 2.8 (MassiveCore 2.8)
  * Factions 2.7 (MassiveCore 2.7)
  * Factions 2.6 (MassiveCore 7.4)
  * FactionsUUID 1.6 by @drtshock
  * FactionsUUID 1.8 by @externo6
* Advanced command editing
  * RegEx support
  * Enhanced aliasing with full argument support
  * Warmups with options to deny on movement and damage
  * Cooldowns that are saved on reboots
  * Blocking commands within faction territory
  * Add command fees
  * Separate permission group command modifiers with Vault support
  * Add your own permissions
* Wilderness command with full factions support

## Development Builds
Obtain the latest compiled version of FBasics here: [Download Here](https://github.com/Sudzzy/FBasics/raw/master/FBasics/target/FBasics.jar "Download Here")

## Commands

| **Command**          | **Description**                                    |
| -------------------- | -------------------------------------------------- |
| /fbasics reload      | Reloads the plugin                                 |
| /fbasics safepromote | Checks the players previous group before promoting |
| /fbasics version     | View the current version you are using             |
| /fbasics wilderness  | Teleport to a random location in the wilderness    |

## Permissions

| **Permission**                              | **Description**                       | **Default** |
| ------------------------------------------- | ------------------------------------- | ----------- |
| fbasics.antiglitch.phase                    | Bypass phase protection               | operator    |
| fbasics.antiglitch.faction-map              | Bypass faction map protection         | operator    |
| fbasics.antiglitch.dismount-clipping        | Bypass dismount clipping protection   | operator    |
| fbasics.antiglitch.nether-roof              | Bypass nether roof protection         | operator    |
| fbasics.antiglitch.mcmmo-mining             | Bypass mcmmo mining protection        | operator    |
| fbasics.antiglitch.inventory-dupe           | Bypass inventory dupe protection      | operator    |
| fbasics.antiglitch.crop-dupe                | Bypass crop dupe protection           | operator    |
| fbasics.antiglitch.book-limit               | Bypass book limit protection          | operator    |
| fbasics.antiglitch.enderpearls-cooldown     | Bypass the enderpearl cooldown        | operator    |
| fbasics.antiglitch.enderpearls-within-block | Bypass enderpearl block protection    | operator    |
| fbasics.antiglitch.enderpearls-factions     | Bypass enderpearl factions protection | operator    |
| fbasics.antilooter                          | Bypass loot protection                | operator    |
| fbasics.commands.cooldowns                  | Bypass command cooldowns              | operator    |
| fbasics.commands.warmups                    | Bypass command warmups                | operator    |
| fbasics.commands.warmups-move               | Bypass command warmup move cancel     | operator    |
| fbasics.commands.warmups-damage             | Bypass command warmup damage cancel   | operator    |
| fbasics.commands.prices                     | Bypass command fees                   | operator    |
| fbasics.commands.factions                   | Bypass command faction limitations    | operator    |
| fbasics.reload                              | Reload the plugin                     | operator    |
| fbasics.safepromote                         | Use the safepromote command           | operator    |
| fbasics.wilderness                          | Use the wilderness command            | everyone    |