package org.soraworld.lockette.event;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignSendEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private String[] lines;
    private boolean modified = false;

    public SignSendEvent(Player player, String[] lines) {
        this.player = player;
        this.lines = lines;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getLine(int linenumber) {
        return lines[linenumber];
    }

    public String[] getLines() {
        return lines;
    }

    public WrappedChatComponent[] getLinesWrappedChatComponent() {
        WrappedChatComponent[] wrappedChatComponent = new WrappedChatComponent[4];
        for (int i = 0; i < 4; i++) {
            wrappedChatComponent[i] = WrappedChatComponent.fromJson(lines[i]);
        }
        return wrappedChatComponent;
    }

    public void setLine(int linenumber, String text) {
        lines[linenumber] = text;
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
