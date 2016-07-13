package org.soraworld.lockette;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;

public class BlockEnvironmentListener implements Listener {

    // Prevent explosion break block
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (Config.isExplosionProtectionDisabled()) return;
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (LocketteAPI.isProtected(block)) it.remove();
        }
    }
//	// Bukkit-1.7.10 has not this method
//	@EventHandler(priority = EventPriority.HIGH)
//	public void onBlockExplode(BlockExplodeEvent event){
//		if (Config.isExplosionProtectionDisabled()) return;
//		Iterator<Block> it = event.blockList().iterator();
//        while (it.hasNext()) {
//            Block block = it.next();
//            if (LocketteAPI.isProtected(block)) it.remove();
//        }
//	}

    // Prevent tree break block
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState blockstate : event.getBlocks()) {
            if (LocketteAPI.isProtected(blockstate.getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Prevent piston extend break block
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (LocketteAPI.isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Prevent piston retract break block
    // Bukkit-1.7.10 dose not support event.getBlocks()
    // 活塞收回事件
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getRetractLocation().getBlock();
        if (LocketteAPI.isProtected(block)) {
            event.setCancelled(true);
        }
    }

    // Prevent redstone current open doors
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (LocketteAPI.isProtected(event.getBlock())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

}
