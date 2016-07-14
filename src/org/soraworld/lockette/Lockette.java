package org.soraworld.lockette;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.soraworld.lockette.api.LocketteAPI;
import org.soraworld.lockette.config.Config;
import org.soraworld.lockette.dependency.Dependency;
import org.soraworld.lockette.dependency.DependencyProtocolLib;
import org.soraworld.lockette.listener.*;
import org.soraworld.lockette.log.Logger;
import org.soraworld.lockette.util.Utils;
import org.soraworld.lockette.util.Version;

public class Lockette extends JavaPlugin {

    private static Plugin plugin;
    private static Version version = Version.UNKNOWN;
    private boolean debug = false;

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Version getBukkitVersion() {
        return version;
    }

    public void onEnable() {
        plugin = this;
        // Read config
        new Config(this);
        // Create Logger
        new Logger(this);
        // Register Listeners
        // If debug mode is not on, debug listener won't register
        if (debug) getServer().getPluginManager().registerEvents(new BlockDebugListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockEnvironmentListener(), this);
        getServer().getPluginManager().registerEvents(new BlockInventoryMoveListener(), this);
        // Dependency
        new Dependency(this);

        if (Config.isUuidEnabled()) {
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                DependencyProtocolLib.setUpProtocolLib(this);
                getServer().getPluginManager().registerEvents(new SignSendListener(), this);
            } else {
                plugin.getLogger().info("ProtocolLib is not found!");
                plugin.getLogger().info("UUID support requires ProtocolLib, or else signs will be ugly!");
            }
        }
    }

    public void onDisable() {
        if (Config.isUuidEnabled() && Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            DependencyProtocolLib.cleanUpProtocolLib(this);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args) {
        if (cmd.getName().equals("lockette")) {
            if (args.length == 0) {
                Utils.sendMessages(sender, Config.getLang("command-usage"));
            } else {
                // The following commands does not require player
                switch (args[0]) {
                    case "reload":
                        if (sender.hasPermission("lockette.reload")) {
                            Config.reload();
                            Utils.sendMessages(sender, Config.getLang("config-reloaded"));
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                    case "version":
                        if (sender.hasPermission("lockette.version")) {
                            sender.sendMessage(plugin.getDescription().getFullName());
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                }
                // The following commands requires player
                if (!(sender instanceof Player)) {
                    Utils.sendMessages(sender, Config.getLang("command-usage"));
                    return false;
                }
                Player player = (Player) sender;
                switch (args[0]) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        if (player.hasPermission("lockette.edit")) {
                            String message = "";
//    					if (args.length == 1){
//    						message = "";
//    					}
                            Block block = Utils.getSelectedSign(player);
                            if (block == null) {
                                Utils.sendMessages(player, Config.getLang("no-sign-selected"));
                            } else if (!LocketteAPI.isSign(block) || !(player.hasPermission("lockette.edit.admin") || LocketteAPI.isOwnerOfSign(block, player))) {
                                Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                            } else {
                                for (int i = 1; i < args.length; i++) {
                                    message += args[i];
                                }
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                if (message.length() > 16) {
                                    Utils.sendMessages(player, Config.getLang("line-is-too-long"));
                                    return true;
                                }
                                if (LocketteAPI.isLockSign(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                            break;
                                        case "2":
                                            if (!player.hasPermission("lockette.admin.edit")) {
                                                Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                                break;
                                            }
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message);
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            if (Config.isUuidEnabled()) {
                                                Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0]) - 1);
                                            }
                                            break;
                                    }
                                } else if (LocketteAPI.isAdditionalSign(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                            break;
                                        case "2":
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message);
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            if (Config.isUuidEnabled()) {
                                                Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0]) - 1);
                                            }
                                            break;
                                    }
                                } else {
                                    Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                                }
                            }
                        } else {
                            Utils.sendMessages(player, Config.getLang("no-permission"));
                        }
                        break;
                    case "force":
                        if (debug && player.hasPermission("lockette.debug")) {
                            Utils.setSignLine(Utils.getSelectedSign(player), Integer.parseInt(args[1]), args[2]);
                            break;
                        }
                    case "update":
                        if (debug && player.hasPermission("lockette.debug")) {
                            Utils.updateSign(Utils.getSelectedSign(player));
                            break;
                        }
                    case "uuid":
                        if (debug && player.hasPermission("lockette.debug")) {
                            Utils.updateUuidOnSign(Utils.getSelectedSign(player));
                            break;
                        }
                    default:
                        Utils.sendMessages(player, Config.getLang("command-usage"));
                        break;
                }
            }
        }
        return true;
    }

}
