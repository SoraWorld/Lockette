package org.soraworld.lockette.util;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.soraworld.lockette.Lockette;
import org.soraworld.lockette.api.LocketteAPI;
import org.soraworld.lockette.config.Config;
import org.soraworld.lockette.log.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Utils {

    public static final String usernamePattern = "^[a-zA-Z0-9_]*$";

    private static Map<Player, Block> selectedSign = new HashMap<>();
    private static Set<Player> notified = new HashSet<>();

    // Helper functions
    @SuppressWarnings("deprecation")
    public static void putSignOn(Block block, BlockFace blockface, String line1, String line2) {
        Block newSign = block.getRelative(blockface);
        newSign.setType(Material.WALL_SIGN);
        byte data = 0;
        // So this part is pretty much a Bukkit bug:
        // Signs' rotation is not correct with bukkit's set facing, below is the workaround.
        switch (blockface) {
            case NORTH:
                data = 2;
                break;
            case EAST:
                data = 5;
                break;
            case WEST:
                data = 4;
                break;
            case SOUTH:
                data = 3;
                break;
            default:
                return;
        }
        newSign.setData(data, true);
        updateSign(newSign);
        Sign sign = (Sign) newSign.getState();
        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.update();
    }

    public static void setSignLine(Block block, int line, String text) {
        // Requires isSign
        Sign sign = (Sign) block.getState();
        sign.setLine(line, text);
        sign.update();
    }

    public static void removeASign(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.getItemInHand().getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }
    }

    public static void updateSign(Block block) {
        block.getState().update();
    }

    public static Block getSelectedSign(Player player) {
        return selectedSign.get(player);
    }

    public static void selectSign(Player player, Block block) {
        selectedSign.put(player, block);
    }

    public static void playLockEffect(Player player, Block block) {
//		player.playSound(block.getLocation(), Sound.DOOR_CLOSE, 0.3F, 1.4F);
//		player.spigot().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.CRIT, 0, 0, 0.3F, 0.3F, 0.3F, 0.1F, 64, 64);
    }

    public static void playAccessDenyEffect(Player player, Block block) {
//		player.playSound(block.getLocation(), Sound.VILLAGER_NO, 0.3F, 0.9F);
//		player.spigot().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.FLAME, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 64, 64);
    }

    public static void sendMessages(CommandSender sender, String messages) {
        if (messages == null || messages.equals("")) return;
        sender.sendMessage(messages);
    }

    public static boolean shouldNotify(Player player) {
        if (notified.contains(player)) {
            return false;
        } else {
            notified.add(player);
            return true;
        }
    }

    public static boolean hasValidCache(Block block) {
        List<MetadataValue> metaDatas = block.getMetadata("expires");
        if (!metaDatas.isEmpty()) {
            long expires = metaDatas.get(0).asLong();
            if (expires > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }

    public static boolean getAccess(Block block) {
        // Requires hasValidCache()
        List<MetadataValue> metaDatas = block.getMetadata("locked");
        return metaDatas.get(0).asBoolean();
    }

    public static void setCache(Block block, boolean access) {
        block.removeMetadata("expires", Lockette.getPlugin());
        block.removeMetadata("locked", Lockette.getPlugin());
        block.setMetadata("expires", new FixedMetadataValue(Lockette.getPlugin(), System.currentTimeMillis() + Config.getCacheTimeMillis()));
        block.setMetadata("locked", new FixedMetadataValue(Lockette.getPlugin(), access));
    }

    public static void resetCache(Block block) {
        block.removeMetadata("expires", Lockette.getPlugin());
        block.removeMetadata("locked", Lockette.getPlugin());
        for (BlockFace blockface : LocketteAPI.newsFaces) {
            Block relative = block.getRelative(blockface);
            if (relative.getType() == block.getType()) {
                relative.removeMetadata("expires", Lockette.getPlugin());
                relative.removeMetadata("locked", Lockette.getPlugin());
            }
        }
    }

    public static void updateUuidOnSign(Block block) {
        for (int line = 1; line < 4; line++) {
            updateUuidByUsername(block, line);
        }
    }

    public static void updateUuidByUsername(final Block block, final int line) {
        Sign sign = (Sign) block.getState();
        final String original = sign.getLine(line);
        Bukkit.getScheduler().runTaskAsynchronously(Lockette.getPlugin(), () -> {
            String username = original;
            if (username.contains("#")) {
                username = username.split("#")[0];
            }
            if (!isUserName(username)) return;
            String uuid = null;
            Player user = Bukkit.getPlayerExact(username);
            if (user != null) {
                // User is online
                uuid = user.getUniqueId().toString();
            } else {
                // User is not online, fetch string
                uuid = getUuidByUsernameFromMojang(username);
            }
            if (uuid != null) {
                final String towrite = username + "#" + uuid;
                Bukkit.getScheduler().runTask(Lockette.getPlugin(), () -> setSignLine(block, line, towrite));
            }
        });
    }

    public static void updateUsernameByUuid(Block block, int line) {
        Sign sign = (Sign) block.getState();
        String original = sign.getLine(line);
        if (isUsernameUuidLine(original)) {
            String uuid = getUuidFromLine(original);
            Player player = null;
            if (uuid != null) {
                player = Bukkit.getPlayer(UUID.fromString(uuid));
            }
            if (player != null) {
                setSignLine(block, line, player.getName() + "#" + uuid);
            }
        }
    }

    public static void updateLineByPlayer(Block block, int line, Player player) {
        setSignLine(block, line, player.getName() + "#" + player.getUniqueId().toString());
    }

    public static boolean isUserName(String text) {
        return text.length() < 17 && text.length() > 2 && text.matches(usernamePattern);
    }

    // Warning: don't use this in a sync way
    public static String getUuidByUsernameFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String responseString = response.toString();
            JsonObject json = new JsonParser().parse(responseString).getAsJsonObject();
            String rawUUID = json.get("id").getAsString();
            return rawUUID.substring(0, 8) + "-" + rawUUID.substring(8, 12) + "-" + rawUUID.substring(12, 16) + "-" + rawUUID.substring(16, 20) + "-" + rawUUID.substring(20);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.info(ex.toString());
        }
        return null;
    }

    public static boolean isUsernameUuidLine(String text) {
        if (text.contains("#")) {
            String[] splits = text.split("#", 2);
            if (splits[1].length() == 36) {
                return true;
            }
        }
        return false;
    }

    public static String getUsernameFromLine(String text) {
        if (isUsernameUuidLine(text)) {
            return text.split("#", 2)[0];
        } else {
            return text;
        }
    }

    public static String getUuidFromLine(String text) {
        if (isUsernameUuidLine(text)) {
            return text.split("#", 2)[1];
        } else {
            return null;
        }
    }

    public static boolean isPlayerOnLine(Player player, String text) {
        if (Utils.isUsernameUuidLine(text)) {
            if (Config.isUuidEnabled()) {
                return player.getUniqueId().toString().equals(getUuidFromLine(text));
            } else {
                return player.getName().equals(getUsernameFromLine(text));
            }
        } else {
            return text.equals(player.getName());
        }
    }

    public static String getSignLineFromUnknown(WrappedChatComponent rawline) {
        String json = rawline.getJson();
        return getSignLineFromUnknown(json);
    }

    public static String getSignLineFromUnknown(String json) {
        try { // 1.8-
            JsonObject line = new JsonParser().parse(json).getAsJsonObject();
            return line.get("extra").getAsJsonArray().get(0).getAsString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.info(ex.toString());
        }
        try { // 1.9+
            JsonObject line = new JsonParser().parse(json).getAsJsonObject();
            return line.get("extra").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.info(ex.toString());
        }
        return json;
    }

}
