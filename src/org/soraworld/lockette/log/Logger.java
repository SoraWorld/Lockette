package org.soraworld.lockette.log;

/* Created by Himmelt on 2016/7/14.*/

import org.bukkit.plugin.Plugin;

public final class Logger {

    private static Plugin plugin;

    public Logger(Plugin _plugin) {
        plugin = _plugin;
    }

    public static void info(String msg) {
        plugin.getLogger().info(msg);
    }
}
