package org.soraworld.lockette.listener;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.soraworld.lockette.api.LocketteAPI;
import org.soraworld.lockette.event.SignSendEvent;
import org.soraworld.lockette.util.Utils;

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