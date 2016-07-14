package org.soraworld.lockette.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.soraworld.lockette.api.LocketteAPI;

public class BlockDebugListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDebugClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (p.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Block b = event.getClickedBlock();
            p.sendMessage(ChatColor.GREEN + "===========================");
            p.sendMessage("isLockable: " + formatBoolean(LocketteAPI.isLockable(b)));
            p.sendMessage("isLocked: " + formatBoolean(LocketteAPI.isLocked(b)));
            p.sendMessage(" - isOwner/User: " + formatBoolean(LocketteAPI.isOwner(b, p)) + ChatColor.RESET + "/" + formatBoolean(LocketteAPI.isUser(b, p)));
            p.sendMessage("isLockedSingle: " + formatBoolean(LocketteAPI.isLockedSingleBlock(b, null)));
            p.sendMessage(" - isOwner/UserSingle: " + formatBoolean(LocketteAPI.isOwnerSingleBlock(b, null, p)) + ChatColor.RESET + "/" + formatBoolean(LocketteAPI.isUserSingleBlock(b, null, p)));
            p.sendMessage("isLockedUpDownLockedDoor: " + formatBoolean(LocketteAPI.isUpDownLockedDoor(b)));
            p.sendMessage(" - isOwner/UserSingle: " + formatBoolean(LocketteAPI.isOwnerUpDownLockedDoor(b, p)) + ChatColor.RESET + "/" + formatBoolean(LocketteAPI.isOwnerUpDownLockedDoor(b, p)));

            p.sendMessage("Block: " + b.getType().toString() + " " + b.getTypeId() + ":" + b.getData());

            if (b.getType() == Material.WALL_SIGN) {
                for (String line : ((Sign) b.getState()).getLines()) {
                    p.sendMessage(ChatColor.GREEN + line);
                }
            }
            p.sendMessage(p.getUniqueId().toString());
        }
    }

    public String formatBoolean(boolean tf) {
        if (tf) {
            return ChatColor.GREEN + "true";
        } else {
            return ChatColor.RED + "false";
        }
    }

}


