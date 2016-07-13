package org.soraworld.lockette;

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

//			p.sendMessage("isLockSign: " + formatBoolean(LocketteAPI.isLockSign(b)));
//			if (LocketteAPI.isLockSign(b)){
//				p.sendMessage(" - isOwnerOnSign: " + formatBoolean(LocketteAPI.isOwnerOnSign(b, p.getName())));
//			}
//			p.sendMessage("isAdditionalSign: " + formatBoolean(LocketteAPI.isAdditionalSign(b)));
//			if (LocketteAPI.isAdditionalSign(b)){
//				p.sendMessage(" - isUserOnSign: " + formatBoolean(LocketteAPI.isUserOnSign(b, p.getName())));
//			}
//			p.sendMessage("isContainer: " + formatBoolean(LocketteAPI.isContainer(b)));
            p.sendMessage("Block: " + b.getType().toString() + " " + b.getTypeId() + ":" + b.getData());

//			if (b.getType() == Material.WALL_SIGN){
//				for (Object line : Reflection.signToBaseComponents(b)){
//					Bukkit.broadcastMessage(line.toString());
//				}
//			}
            if (b.getType() == Material.WALL_SIGN) {
//				List<Object> basecomponents = Reflection.signToBaseComponents(b);
//				p.sendMessage("Text:Clickable:Hoverable");
//				for (Object basecomponent : basecomponents){
//					//p.sendMessage(ChatColor.RED + basecomponent.toString());
//					p.sendMessage(ChatColor.YELLOW + Reflection.baseComponentToText(basecomponent) + ":" + Reflection.baseComponentToClickable(basecomponent) + ":" + Reflection.baseComponentToHoverable(basecomponent));
//				}
                for (String line : ((Sign) b.getState()).getLines()) {
                    p.sendMessage(ChatColor.GREEN + line);
                }
//				Object basecomponent = basecomponents.get(0);
//				p.sendMessage(ChatColor.RED + basecomponent.toString());
//				p.sendMessage(ChatColor.YELLOW + Reflection.baseComponentToText(basecomponent) + ":" + Reflection.baseComponentToClickable(basecomponent) + ":" + Reflection.baseComponentToHoverable(basecomponent));
//				p.sendMessage(ChatColor.GREEN + ((Sign)b.getState()).getLines()[0]);
            }
            p.sendMessage(p.getUniqueId().toString());
            //p.sendMessage(Utils.getUuidByUsernameFromMojang(p.getName()));
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


