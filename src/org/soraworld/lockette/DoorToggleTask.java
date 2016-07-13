package org.soraworld.lockette;

import org.bukkit.block.Block;

import java.util.List;

public class DoorToggleTask implements Runnable {

    private List<Block> doors;

    public DoorToggleTask(List<Block> doors_) {
        doors = doors_;
    }

    @Override
    public void run() {
        for (Block door : doors) {
            if (LocketteAPI.isDoubleDoorBlock(door)) {
                Block doorbottom = LocketteAPI.getBottomDoorBlock(door);
                //LocketteAPI.toggleDoor(doorbottom, open);
                LocketteAPI.toggleDoor(doorbottom);
            }
        }
    }

}
