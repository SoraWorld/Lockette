package org.soraworld.lockette.listener;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.soraworld.lockette.api.LocketteAPI;
import org.soraworld.lockette.config.Config;
import org.soraworld.lockette.util.Utils;

public class BlockInventoryMoveListener implements Listener {

    // 漏斗输运事件
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (Config.isItemTransferOutBlocked() || Config.getHopperMinecartAction() != (byte) 0) {
            if (isInventoryLocked(event.getSource())) {
                if (Config.isItemTransferOutBlocked()) {
                    event.setCancelled(true);
                }
                // Additional Hopper Minecart Check
                if (event.getDestination().getHolder() instanceof HopperMinecart) {
                    byte hopperMinecartAction = Config.getHopperMinecartAction();
                    switch (hopperMinecartAction) {
                        // case 0 - Impossible
                        case (byte) 1: // Cancel only, it is not called if !Config.isItemTransferOutBlocked()
                            event.setCancelled(true);
                            break;
                        case (byte) 2: // Extra action - HopperMinecart removal
                            event.setCancelled(true);
                            ((HopperMinecart) event.getDestination().getHolder()).remove();
                            break;
                    }
                }
                return;
            }
        }
        if (Config.isItemTransferInBlocked()) {
            if (isInventoryLocked(event.getDestination())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isInventoryLocked(Inventory inventory) {
        InventoryHolder inventoryholder = inventory.getHolder();
        if (inventoryholder instanceof DoubleChest) {
            inventoryholder = ((DoubleChest) inventoryholder).getLeftSide();
        }
        if (inventoryholder instanceof BlockState) {
            Block block = ((BlockState) inventoryholder).getBlock();
            if (Config.isCacheEnabled()) {
                // Cache is enabled
                if (Utils.hasValidCache(block)) {
                    return Utils.getAccess(block);
                } else {
                    if (LocketteAPI.isLocked(block)) {
                        Utils.setCache(block, true);
                        return true;
                    } else {
                        Utils.setCache(block, false);
                        return false;
                    }
                }
            } else {
                // Cache is disabled
                return LocketteAPI.isLocked(block);
            }
        }
        return false;
    }

}
