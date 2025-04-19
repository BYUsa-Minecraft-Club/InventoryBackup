## Usage
> /invbackup restore

Opens a GUI with an alphabetically sorted list of available players. Select a player's head to view their available backups

> /invbackup restore `PlayerName`

Requires player with name `PlayerName` to be online. Opens a GUI displaying player's backups


> /invbackup forcebackup

Creates a backup for all online players

> /invbackup forcebackup `PlayerName`

Requires player with name `PlayerName` to be online. Creates a backup for player


Currently there is a maximum of 10 backups of each type. If this limit is reached, old backups are deleted.


## Updating
1. Go to [Gradle Update Instructions](https://docs.gradle.org/current/userguide/upgrading_version_8.html) and [Gradle Releases](https://gradle.org/releases/)
2. Check that the version in step 3 of the instructions matches the most recent version of the releases
3. Copy the command from step 3 of the instructions and run it locally
   1. If you have IntelliJ you can click the gear icon in the top right, select `Run Anything`, paste the command, then hit enter
   2. If you do not have IntelliJ but you have gradle installed locally, you can just run it in a terminal in the project
   3. If you don't have either, you may find a different way but I would suggest downloading IntelliJ (free/community edition should be fine)
4. Go to [FabricMC Devlop Page](https://fabricmc.net/develop/) and scroll down to `Latest Versions`
5. Select the version you wish to update to in the box
6. Open [gradle.properties](gradle.properties) locally
7. Copy the values from the box on the Fabric Develop page into `gradle.properties`. The first three are together towards the middle and the last one is at the bottom
8. Also update `mod_version` in `gradle.properties` for the new version of minecraft.
9. Open [src/main/resources/fabric.mod.json](src/main/resources/fabric.mod.json) and change the required minecraft version to desired version (line 25 at time of writing)
10. Open [build.gradle](build.gradle) and scroll down to `dependencies` (about line 50)
11. For any dependencies that may be updated, see if there is an updated version and if there is, replace it with the updated version
    1. For `sgui` you can find it [here](https://github.com/Patbox/sgui/releases)
12. Try to run the client `./gradlew runClient`
    1. If there are any compile errors, try to fix them. I can't give you a ton of guidance here but sometimes Mojang adds parameters to methods, encapsulates parameters, renames methods, etc. Sometimes there are bigger changes that would require more refactoring; if these happens hopefully you can figure it out!
    2. Once you've fixed the compile errors for the file, try to run the client again
13. Once you have the client running, join/make a single-player world and test out various features of the mod
    1. Put some items in your inventory and make sure backups are created successfully in different scenarios (joining, leaving, player death, going through a portal, when `/invbackup forcebackup` is run).
    2. Make sure you can view all the backups correctly and they display how you expect. Make sure you can restore your inventory to a backup after your inventory changes and that it restores correctly
    3. Make sure you can still edit your own live inventory (fun to play with, you can put blocks in your armor slots)
    4. Any other features you think I forgot in this list 
14. Quit the world, quit the game, relaunch the game, rejoin the world (you should be a different player), and test out other features 
    1. Make sure the inventory backups from before quitting are still there and display correctly
    2. See if you can edit the previous player's live inventory and the console doesn't display any stack traces
    3. See if you can restore a backup of the previous player and make sure the console doesn't display any stack traces
15. If something seems broken or not working, please try to fix it. I can't give much guidance here either as I don't know what would break. Good luck!
16. Once you're satisfied that everything works, clean old build artifacts out and build the jar with `./gradlew clean build`
17. Mod jar file should be in `build/libs/inventorybackup-<version>+<mc version>.jar` NOT the `-sources.jar`. 
