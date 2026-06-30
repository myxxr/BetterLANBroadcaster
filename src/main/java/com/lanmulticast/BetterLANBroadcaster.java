package com.lanmulticast;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterLANBroadcaster extends JavaPlugin {

    private MulticastBroadcaster broadcaster;
    private LanguageManager languageManager;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        // Save default config and language files
        saveDefaultConfig();
        saveResourceIfNotExists("lang/messages_en.yml");
        saveResourceIfNotExists("lang/messages_zh.yml");

        // Load language manager
        languageManager = new LanguageManager(this);

        // Load config values
        String motd = getConfig().getString("motd", "A Minecraft Server");
        int delayMs = getConfig().getInt("broadcast-delay-ms", 1500);
        int port = resolvePort();
        boolean debug = getConfig().getBoolean("debug", false);

        // Initialize broadcaster (not started yet until /blb start)
        broadcaster = new MulticastBroadcaster(this, motd, port, delayMs);
        broadcaster.setDebug(debug);

        // Register command handler
        commandHandler = new CommandHandler(this);
        getCommand("betterlanbroadcaster").setExecutor(commandHandler);
        getCommand("betterlanbroadcaster").setTabCompleter(commandHandler);

        getLogger().info("BetterLANBroadcaster has been enabled! Broadcast port: " + port);
    }

    @Override
    public void onDisable() {
        if (broadcaster != null) {
            broadcaster.stop();
        }
        getLogger().info("BetterLANBroadcaster has been disabled.");
    }

    /**
     * Reloads the config and language files, then re-applies settings to the broadcaster.
     */
    public void reloadPluginConfig() {
        reloadConfig();

        // Reload language
        languageManager.loadLanguage();

        // Apply config values
        String motd = getConfig().getString("motd", "A Minecraft Server");
        int delayMs = getConfig().getInt("broadcast-delay-ms", 1500);
        int port = resolvePort();
        boolean debug = getConfig().getBoolean("debug", false);

        broadcaster.setMotd(motd);
        broadcaster.setDelayMs(delayMs);
        broadcaster.setPort(port);
        broadcaster.setDebug(debug);
    }

    /**
     * Resolves the broadcast port:
     * - If config "broadcast-port" > 0, use that value
     * - Otherwise, auto-detect from the server
     */
    private int resolvePort() {
        int configPort = getConfig().getInt("broadcast-port", 0);
        if (configPort > 0 && configPort <= 65535) {
            return configPort;
        }
        return getServer().getPort();
    }

    public MulticastBroadcaster getBroadcaster() {
        return broadcaster;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Saves a resource from the jar to the plugin data folder only if it doesn't exist.
     */
    private void saveResourceIfNotExists(String resource) {
        if (getResource(resource) != null && !new java.io.File(getDataFolder(), resource).exists()) {
            saveResource(resource, false);
        }
    }
}
