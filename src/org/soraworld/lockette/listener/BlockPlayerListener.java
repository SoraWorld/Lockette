package org.soraworld.lockette.listener;

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
import org.soraworld.lockette.Lockette;
import org.soraworld.lockette.api.LocketteAPI;
import org.soraworld.lockette.config.Config;
import org.soraworld.lockette.dependency.Dependency;
import org.soraworld.lockette.task.DoorToggleTask;
import org.soraworld.lockette.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BlockPlayerListener implements Listener {

    // Quick protect for chests
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuickLockChest(PlayerInteractEvent event) {

        if (event.isCancelled()) return;
        Action action = event.getAction();
        Player player = event.getPlayer();
        // 右键手持木牌
        if (action == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.SIGN) {
            // 潜行方式,取消
            if (player.isSneaking()) return;
            // 如果玩家没有使用权限,取消
            if (!player.hasPermission("lockette.lock")) return;
            BlockFace blockface = event.getBlockFace();
            // 木牌只能贴在四个侧面
            if (blockface == BlockFace.NORTH || blockface == BlockFace.WEST || blockface == BlockFace.EAST || blockface == BlockFace.SOUTH) {
                Block block = event.getClickedBlock();
                // 检查是否被其他插件保护,若不被该玩家保护则取消
                if (Dependency.isProtectedFrom(block, player)) return;
                // 检查贴牌子的一面是否被占用
                if (block.getRelative(blockface).getType() != Material.AIR) return;
                // 在被保护方块列表内
                if (LocketteAPI.isLockable(block)) {
                    boolean locked = LocketteAPI.isLocked(block);
                    // 取消交互事件
                    event.setCancelled(true);
                    if (!locked && !LocketteAPI.isUpDownLockedDoor(block)) {
                        // 拿掉玩家一个木牌
                        Utils.removeASign(player);
                        // 显示消息
                        Utils.sendMessages(player, Config.getLang("locked-quick"));
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
        if (!player.hasPermission("lockette.lock")) {
            if (LocketteAPI.isLockString(topline) || LocketteAPI.isAdditionalString(topline)) {
                event.setLine(0, Config.getLang("sign-error"));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
            }
            return;
        }
        if (LocketteAPI.isLockString(topline) || LocketteAPI.isAdditionalString(topline)) {
            Block block = LocketteAPI.getAttachedBlock(event.getBlock());
            if (LocketteAPI.isLockable(block)) {
                // 检查其他插件保护
                if (Dependency.isProtectedFrom(block, player)) {
                    event.setLine(0, Config.getLang("sign-error"));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return;
                }
                boolean locked = LocketteAPI.isLocked(block);
                if (!locked && !LocketteAPI.isUpDownLockedDoor(block)) {
                    if (LocketteAPI.isLockString(topline)) {
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("lockette.lockothers")) {
                            // Player with permission can lock with another name
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
                } else {
                    // Not possible to fall here except override
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
            if (!player.hasPermission("lockette.edit")) return;
            if (LocketteAPI.isOwnerOfSign(block, player) || (LocketteAPI.isLockSignOrAdditionalSign(block) && player.hasPermission("lockette.edit.admin"))) {
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
        if (player.hasPermission("lockette.admin.break")) return;
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
        Action action = event.getAction();
        switch (action) {
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();
                if (((LocketteAPI.isLocked(block) && !LocketteAPI.isUser(block, player)) ||
                        (LocketteAPI.isUpDownLockedDoor(block) && !LocketteAPI.isUserUpDownLockedDoor(block, player)))
                        && !player.hasPermission("lockette.admin.use")) {
                    Utils.sendMessages(player, Config.getLang("block-is-locked"));
                    event.setCancelled(true);
                    Utils.playAccessDenyEffect(player, block);
                } else {
                    // Handle double doors
                    if (action == Action.RIGHT_CLICK_BLOCK) {
                        if (LocketteAPI.isDoubleDoorBlock(block) && LocketteAPI.isLocked(block)) {
                            Block doorBlock = LocketteAPI.getBottomDoorBlock(block);
                            BlockState doorState = doorBlock.getState();
                            Openable openableState = (Openable) doorState.getData();
                            boolean shouldOpen = !openableState.isOpen(); // Move to here
                            int closetime = LocketteAPI.getTimerDoor(doorBlock);
                            List<Block> doors = new ArrayList<>();
                            doors.add(doorBlock);
                            if (doorBlock.getType() == Material.IRON_DOOR_BLOCK) {
                                LocketteAPI.toggleDoor(doorBlock, shouldOpen);
                            }
                            for (BlockFace blockface : LocketteAPI.newsFaces) {
                                Block relative = doorBlock.getRelative(blockface);
                                if (relative.getType() == doorBlock.getType()) {
                                    doors.add(relative);
                                    LocketteAPI.toggleDoor(relative, shouldOpen);
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
        if (player.hasPermission("lockette.admin.interfere")) return;
        if (LocketteAPI.mayInterfere(block, player)) {
            Utils.sendMessages(player, Config.getLang("cannot-interfere-with-others"));
            event.setCancelled(true);
            Utils.playAccessDenyEffect(player, block);
        }
    }

    // Tell player about lockette
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlaceFirstBlockNotify(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!player.hasPermission("lockette.lock")) return;
        if (Utils.shouldNotify(player) && Config.isLockable(block.getType())) {
            Utils.sendMessages(player, Config.getLang("you-can-quick-lock-it"));
        }
    }

}
