package org.soraworld.lockette;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SignSendListener implements Listener {

    @EventHandler
    public void onSignSend(SignSendEvent event) {
        if (LocketteAPI.isLockStringOrAdditionalString(Utils.getSignLineFromUnknown(event.getLine(0)))) {
            for (int i = 1; i < 4; i++) {
                String line = Utils.getSignLineFromUnknown(event.getLine(i));
                if (Utils.isUsernameUuidLine(line)) {
                    event.setLine(i, WrappedChatComponent.fromText(Utils.getUsernameFromLine(line)).getJson());
                }
            }
        }
    }

}