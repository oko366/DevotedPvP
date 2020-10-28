# DevotedPvP
Core plugin for the devoted pvp server

!IMPORTANT! THIS IS AN EXPERIMENTAL 1.16 BUILD. !IMPORTANT!

Features:

* ELO tracking

* Duels, arena setting for duels

* Kit load / save

* Sanitized Kits (no cheaty kits)

* External integration supports

and more...

See example configs for how to use


## Sanitized Kits

Turn off all sanitization, useful if you've got a lot of custom stuff or only allow kitbuilding via chest / command block, just set
`cleanInventories: false` in config.yml. We strongly recommend leaving it on if you allow gamemode 1 kitbuilding as it will restrict to
valid, vanilla kitouts only and prevent exploitative blocks, items, and enchants.

If you have specific items that should be exempted, consider creating a mod that listens for com.biggestnerd.devotedpvp.ItemSafeEvent . Call "setValid()" on the event if the ItemStack it holds should be permitted.

Kits are sanitized on save, load, and when entering a duel.

## Command quick reference

### Admin commands

`warpmanage` - `wm` alias - `pvp.badmin` perm (default op):
    `/wm first <name>` Define where you are standing as the first warp point for <name>'d warp.
    `/wm second <name>` Define where you are standing as the second warp point for <name>'d warp.
    `/wm delete <name>` Remove all warp points for the <name>'d warp.
    Please note that warps are saved on plugin shutdown.

### Player commands

`clear` - `pvp.duel` perm:
    `/clear` Removes all items from your inventory.

`inv` - `pvp.use` perm:
    `/inv save <name>` Saves your current inventory to <name>. <name>s must be unique across all players.
    `/inv load <name>` Loads inventory previously saved as <name>.
    `/inv delete <name>` - `/inv del <name>` Deletes inventory saved as <name>, if you were the player who created it.

`duel` - `pvp.duel` perm:
    `/duel <player>` Sends <player> a request for a duel.

`accept` - `pvp.duel` perm:
    `/accept <player>` Accepts a duel request from <player> assuming they are online.

`forfeit` - `ff` / `surrender` alias - `pvp.use` perm:
    `/ff` Forfeits an active duel. Only works if you are actually in a duel.

`spectate` - `pvp.use` perm:
    `/spectate` Toggles spectator mode. If Vault is installed, will also give or remove specific permissions as configured in config.yml .

`elo` - `rank` alias - `pvp.duel` perm:
    `/elo` Checks your ELO.

## Config quick reference

Just, read through config.yml -- plenty of examples and descriptions there. Check in folder src/main/resources/config.yml .

One small important thing, the World Spawn location is the lobby location, to change lobby locations change the world spawn location. 
