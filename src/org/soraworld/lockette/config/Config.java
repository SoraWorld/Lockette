package org.soraworld.lockette.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {

    private static Plugin plugin;
    private static FileConfiguration langFile;
    private static boolean enableUUID = false;
    private static Set<Material> lockables = new HashSet<>();
    private static Set<String> privateSigns = new HashSet<>();
    private static Set<String> moreSigns = new HashSet<>();
    private static Set<String> everySigns = new HashSet<>();
    private static Set<String> timerSigns = new HashSet<>();
    private static String defaultPrivateSign = "[Private]";
    private static String defaultMoresSign = "[More Users]";
    private static boolean blockInterferePlacement = true;
    private static boolean blockItemTransferIn = false;
    private static boolean blockItemTransferOut = false;
    private static boolean explosionProtection = true;
    private static int cacheTime = 0;
    private static boolean enableCache = false;
    private static byte blockHopperMinecart = 0;//漏斗矿车

    public Config(Plugin _plugin) {
        plugin = _plugin;
        reload();
    }

    @SuppressWarnings("deprecation")
    public static void reload() {
        plugin.saveDefaultConfig();
        initConfigFiles();
        FileConfiguration configFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        enableUUID = configFile.getBoolean("enable-uuid", false);
        String langName = configFile.getString("language-file", "lang_en_us.yml");
        langFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), langName));

        blockInterferePlacement = configFile.getBoolean("block-interfere-placement", true);
        blockItemTransferIn = configFile.getBoolean("block-item-transfer-in", false);
        blockItemTransferOut = configFile.getBoolean("block-item-transfer-out", true);
        explosionProtection = configFile.getBoolean("explosion-protection", true);

        List<String> privateSignList = configFile.getStringList("private-signs");
        List<String> moreSignList = configFile.getStringList("more-signs");
        List<String> everySignList = configFile.getStringList("everyone-signs");

        privateSigns = new HashSet<>(privateSignList);
        moreSigns = new HashSet<>(moreSignList);
        everySigns = new HashSet<>(everySignList);
        defaultPrivateSign = privateSignList.get(0);
        defaultMoresSign = moreSignList.get(0);

        List<String> timerSignList = configFile.getStringList("timer-signs");
        List<String> timerSignList2 = timerSignList.stream().filter(timerString -> timerString.contains("@")).collect(Collectors.toList());
        timerSigns = new HashSet<>(timerSignList2);

        cacheTime = configFile.getInt("cache-time-seconds", 0) * 1000;
        enableCache = (configFile.getInt("cache-time-seconds", 0) > 0);
        if (enableCache) {
            plugin.getLogger().info("You have cache enabled!");
            plugin.getLogger().info("This is currently for experimental purpose only!");
        }

        String blockHopperMinecartString = configFile.getString("block-hopper-minecart", "remove");
        switch (blockHopperMinecartString.toLowerCase()) {
            case "true":
                blockHopperMinecart = 1;
                break;
            case "false":
                blockHopperMinecart = 0;
                break;
            case "remove":
                blockHopperMinecart = 2;
                break;
            default:
                blockHopperMinecart = 2;
                break;
        }
        // 未处理的？
        List<String> unprocessedItems = configFile.getStringList("lockables");
        lockables = new HashSet<>();
        for (String unprocessedItem : unprocessedItems) {
            if (unprocessedItem.equals("*")) {
                Collections.addAll(lockables, Material.values());
                plugin.getLogger().info("All blocks are default to be lockable!");
                plugin.getLogger().info("Add '-<Material>' to exempt a block, such as '-STONE'!");
                continue;
            }
            boolean add = true;
            if (unprocessedItem.startsWith("-")) {
                add = false;
                unprocessedItem = unprocessedItem.substring(1);
            }
            try { // Is it a number?
                int materialId = Integer.parseInt(unprocessedItem);
                // Hit here without error means yes it is
                if (add) {
                    lockables.add(Material.getMaterial(materialId));
                } else {
                    lockables.remove(Material.getMaterial(materialId));
                }
            } catch (Exception ex) {
                // It is not really a number...
                Material material = Material.getMaterial(unprocessedItem);
                if (material == null) {
                    plugin.getLogger().info(unprocessedItem + " is not an item!");
                } else {
                    if (add) {
                        lockables.add(material);
                    } else {
                        lockables.remove(material);
                    }
                }
            }
        }
        lockables.remove(Material.WALL_SIGN);
    }

    public static void initConfigFiles() {
        String[] langFiles = {"lang_zh_cn.yml", "lang_en_us.yml"};
        for (String filename : langFiles) {
            File _file = new File(plugin.getDataFolder(), filename);
            if (!_file.exists()) {
                plugin.saveResource(filename, false);
            }
        }
    }

    public static boolean isInterferePlacementBlocked() {
        return blockInterferePlacement;
    }

    public static boolean isItemTransferInBlocked() {
        return blockItemTransferIn;
    }

    public static boolean isItemTransferOutBlocked() {
        return blockItemTransferOut;
    }

    public static byte getHopperMinecartAction() {
        return blockHopperMinecart;
    }

    public static String getLang(String path) {
        return ChatColor.translateAlternateColorCodes('&', langFile.getString(path, ""));
    }

    public static boolean isUuidEnabled() {
        return enableUUID;
    }

    public static boolean isLockable(Material material) {
        return lockables.contains(material);
    }

    public static boolean isPrivateSignString(String message) {
        return privateSigns.contains(message);
    }

    public static boolean isMoreSign(String message) {
        return moreSigns.contains(message);
    }

    public static boolean isEveryoneSignString(String message) {
        return everySigns.contains(message);
    }

    public static boolean isExplosionProtection() {
        return explosionProtection;
    }

    public static boolean isTimerSignString(String message) {
        for (String timerString : timerSigns) {
            String[] splits = timerString.split("@", 2);
            if (message.startsWith(splits[0]) && message.endsWith(splits[1])) {
                return true;
            }
        }
        return false;
    }

    public static int getTimer(String message) {
        for (String timerString : timerSigns) {
            String[] splits = timerString.split("@", 2);
            if (message.startsWith(splits[0]) && message.endsWith(splits[1])) {
                String newMessage = message.replace(splits[0], "").replace(splits[1], "");
                try {
                    int seconds = Integer.parseInt(newMessage);
                    return Math.min(seconds, 20);
                } catch (Exception ex) {
                    plugin.getLogger().info(ex.toString());
                }
            }
        }
        return 0;
    }

    public static String getDefaultPrivateString() {
        return defaultPrivateSign;
    }

    public static String getDefaultAdditionalString() {
        return defaultMoresSign;
    }

    public static int getCacheTimeMillis() {
        return cacheTime;
    }

    public static boolean isCacheEnabled() {
        return enableCache;
    }

}
