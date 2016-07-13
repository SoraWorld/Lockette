package org.soraworld.lockette;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Openable;

import java.util.ArrayList;
import java.util.List;

public class BlockPlayerListener implements Listener {

    // Quick protect for chests
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuickLockChest(PlayerInteractEvent event) {

        if (event.isCancelled()) return;
        // 不使用快速保护,取消
        if (Config.getQuickProtectAction() == (byte) 0) return;
        Action action = event.getAction();
        Player player = event.getPlayer();
        // 右键手持木牌
        if (action == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.SIGN) {
            // 不是 [潜行+保护方式2 || 非潜行+保护方式1] 则取消
            if (!((event.getPlayer().isSneaking() && Config.getQuickProtectAction() == (byte) 2) ||
                    (!event.getPlayer().isSneaking() && Config.getQuickProtectAction() == (byte) 1))) return;
            // 如果玩家没有使用权限,取消
            if (!player.hasPermission("lockette.lock")) return;
            BlockFace blockface = event.getBlockFace();
            // 木牌只能贴在四个侧面
            if (blockface == BlockFace.NORTH || blockface == BlockFace.WEST || blockface == BlockFace.EAST || blockface == BlockFace.SOUTH) {
                Block block = event.getClickedBlock();
                // 检查是否被其他插件保护,若不被该玩家保护则取消
                if (Dependency.isProtectedFrom(block, player)) return; // External check here
                if (block.getRelative(blockface).getType() != Material.AIR) return; // This location is obstructed
                // 被保护方块内
                if (LocketteAPI.isLockable(block)) {
                    boolean locked = LocketteAPI.isLocked(block);
                    // 取消事件
                    event.setCancelled(true);
                    if (!locked && !LocketteAPI.isUpDownLockedDoor(block)) {
                        // 拿掉玩家一个木牌
                        Utils.removeASign(player);
                        // 显示消息
                        Utils.sendMessages(player, Config.getLang("locked-quick"));
                        Lockette.getPlugin().getLogger().info("3!!!!!!!!!!!:" + Config.getDefaultPrivateString());
                        Utils.putSignOn(block, blockface, Config.getDefaultPrivateString(), player.getName());

                        Utils.resetCache(block);
                        if (Config.isUuidEnabled()) {
                            Utils.updateLineByPlayer(block.getRelative(blockface), 1, player);
                        }
                    } else if (!locked && LocketteAPI.isOwnerUpDownLockedDoor(block, player)) {
                        Utils.removeASign(player);
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                        Utils.putSignOn(block, blockface, Config.getDefaultAdditionalString(), "");
                    } else if (LocketteAPI.isOwner(block, player)) {
                        Utils.removeASign(player);
                        Lockette.getPlugin().getLogger().info("4!!!!!!!!!!!:" + Config.getDefaultPrivateString());
                        Utils.putSignOn(block, blockface, Config.getDefaultAdditionalString(), "");
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
                    }
                }
            }
        }
    }

    // Manual protection
    @EventHandler(priority = EventPriority.NORMAL)
    public void onManualLock(SignChangeEvent event) {
        if (event.getBlock().getType() != Material.WALL_SIGN) return;
        String topline = event.getLine(0);
        Player player = event.getPlayer();
        if (!player.hasPermission("lockettepro.lock")) {
            if (LocketteAPI.isLockString(topline) || LocketteAPI.isAdditionalString(topline)) {
                event.setLine(0, Config.getLang("sign-error"));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
            }
            return;
        }
        if (LocketteAPI.isLockString(topline) || LocketteAPI.isAdditionalString(topline)) {
            Block block = LocketteAPI.getAttachedBlock(event.getBlock());
            if (LocketteAPI.isLockable(block)) {
                if (Dependency.isProtectedFrom(block, player)) { // External check here
                    event.setLine(0, Config.getLang("sign-error"));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return;
                }
                boolean locked = LocketteAPI.isLocked(block);
                if (!locked && !LocketteAPI.isUpDownLockedDoor(block)) {
                    if (LocketteAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("lockettepro.lockothers")) { // Player with permission can lock with another name
                            event.setLine(1, player.getName());
                        }
                        Utils.resetCache(block);
                    } else {
                        Utils.sendMessages(player, Config.getLang("not-locked-yet-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    }
                } else if (!locked && LocketteAPI.isOwnerUpDownLockedDoor(block, player)) {
                    if (LocketteAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("cannot-lock-door-nearby-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else if (LocketteAPI.isOwner(block, player)) {
                    if (LocketteAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else { // Not possible to fall here except override
                    Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                    event.getBlock().breakNaturally();
                    Utils.playAccessDenyEffect(player, block);
                }
            } else {
                Utils.sendMessages(player, Config.getLang("block-is-not-lockable"));
                event.setLine(0, Config.getLang("sign-error"));
                Utils.playAccessDenyEffect(player, block);
            }
        }
    }

    // Player select sign
    @EventHandler(priority = EventPriority.LOW)
    public void playerSelectSign(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WALL_SIGN) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (!player.hasPermission("lockettepro.edit")) return;
            if (LocketteAPI.isOwnerOfSign(block, player) || (LocketteAPI.isLockSignOrAdditionalSign(block) && player.hasPermission("lockettepro.edit.admin"))) {
                Utils.selectSign(player, block);
                Utils.sendMessages(player, Config.getLang("sign-selected"));
                Utils.playLockEffect(player, block);
            }
        }
    }

    // Player break sign
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakSign(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("lockettepro.admin.break")) return;
        if (LocketteAPI.isLockSign(block)) {
            if (LocketteAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-lock-sign"));
                Utils.resetCache(LocketteAPI.getAttachedBlock(block));
                // Remove additional signs?
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-lock-sign"));
                event.setCancelled(true);
                Utils.playAccessDenyEffect(player, block);
            }
        } else if (LocketteAPI.isAdditionalSign(block)) {
            if (LocketteAPI.isOwnerOfSign(block, player)) {
                Utils.sendMessages(player, Config.getLang("break-own-additional-sign"));
            } else if (!LocketteAPI.isProtected(LocketteAPI.getAttachedBlock(block))) {
                Utils.sendMessages(player, Config.getLang("break-redundant-additional-sign"));
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-additional-sign"));
                event.setCancelled(true);
                Utils.playAccessDenyEffect(player, block);
            }
        }
    }

    // Protect block from being destroyed
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakLockedBlocks(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (LocketteAPI.isLocked(block) || LocketteAPI.isUpDownLockedDoor(block)) {
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
            Utils.playAccessDenyEffect(player, block);
        }
    }

    // Protect block from being used & handle double doors
    // Bukkit-1.7.10 dose not support event.getHand()
    // 保护方块被使用
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptInteractLockedBlocks(PlayerInteractEvent event) {
//		if (Lockette.needCheckHand()){
//			if (event.getHand() != EquipmentSlot.HAND) return;
//		}
        Action action = event.getAction();
        switch (action) {
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();
                if (((LocketteAPI.isLocked(block) && !LocketteAPI.isUser(block, player)) ||
                        (LocketteAPI.isUpDownLockedDoor(block) && !LocketteAPI.isUserUpDownLockedDoor(block, player)))
                        && !player.hasPermission("lockettepro.admin.use")) {
                    Utils.sendMessages(player, Config.getLang("block-is-locked"));
                    event.setCancelled(true);
                    Utils.playAccessDenyEffect(player, block);
                } else { // Handle double doors
                    if (action == Action.RIGHT_CLICK_BLOCK) {
                        if (LocketteAPI.isDoubleDoorBlock(block) && LocketteAPI.isLocked(block)) {
                            Block doorblock = LocketteAPI.getBottomDoorBlock(block);
                            BlockState doorstate = doorblock.getState();
                            Openable openablestate = (Openable) doorstate.getData();
                            boolean shouldopen = !openablestate.isOpen(); // Move to here
                            int closetime = LocketteAPI.getTimerDoor(doorblock);
                            List<Block> doors = new ArrayList<Block>();
                            doors.add(doorblock);
                            if (doorblock.getType() == Material.IRON_DOOR_BLOCK) {
                                LocketteAPI.toggleDoor(doorblock, shouldopen);
                            }
                            for (BlockFace blockface : LocketteAPI.newsfaces) {
                                Block relative = doorblock.getRelative(blockface);
                                if (relative.getType() == doorblock.getType()) {
                                    doors.add(relative);
                                    LocketteAPI.toggleDoor(relative, shouldopen);
                                }
                            }
                            if (closetime > 0) {
                                Bukkit.getScheduler().runTaskLater(Lockette.getPlugin(), new DoorToggleTask(doors), closetime * 20);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    // Protect block from interfere block
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptPlaceInterfereBlocks(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("lockettepro.admin.interfere")) return;
        if (LocketteAPI.mayInterfere(block, player)) {
            Utils.sendMessages(player, Config.getLang("cannot-interfere-with-others"));
            event.setCancelled(true);
            Utils.playAccessDenyEffect(player, block);
        }
    }

    // Tell player about lockettepro
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlaceFirstBlockNotify(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!player.hasPermission("lockettepro.lock")) return;
        if (Utils.shouldNotify(player) && Config.isLockable(block.getType())) {
            switch (Config.getQuickProtectAction()) {
                case (byte) 0:
                    Utils.sendMessages(player, Config.getLang("you-can-manual-lock-it"));
                    break;
                case (byte) 1:
                case (byte) 2:
                    Utils.sendMessages(player, Config.getLang("you-can-quick-lock-it"));
                    break;
            }
        }
    }

}
