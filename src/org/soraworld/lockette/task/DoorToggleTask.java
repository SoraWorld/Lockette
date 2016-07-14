package org.soraworld.lockette.task;

import org.bukkit.block.Block;
import org.soraworld.lockette.api.LocketteAPI;

import java.util.List;

public class DoorToggleTask implements Runnable {

    private List<Block> doors;

    public DoorToggleTask(List<Block> doors_) {
        doors = doors_;
    }

    @Override
    public void run() {
        //LocketteAPI.toggleDoor(doorBottom, open);
        doors.stream().filter(LocketteAPI::isDoubleDoorBlock).forEach(door -> {
            Block doorBottom = LocketteAPI.getBottomDoorBlock(door);
            //LocketteAPI.toggleDoor(doorBottom, open);
            LocketteAPI.toggleDoor(doorBottom);
        });
    }

}
