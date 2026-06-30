package com.lanmulticast;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

public class LanguageManager {

    private static final String LANG_DIR = "lang";

    private final JavaPlugin plugin;
    private YamlConfiguration messages;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    /**
     * Loads the language file based on the config setting.
     */
    public void loadLanguage() {
        String lang = plugin.getConfig().getString("language", "en");
        String fileName = "messages_" + lang + ".yml";
        String resourcePath = LANG_DIR + "/" + fileName;
        File langFile = new File(plugin.getDataFolder(), LANG_DIR + File.separator + fileName);

        if (!langFile.exists()) {
            // Try to save the built-in language file
            try (InputStream in = plugin.getResource(resourcePath)) {
                if (in != null) {
                    plugin.saveResource(resourcePath, false);
                }
            } catch (Exception ignored) {
            }
        }

        // Load from file if exists, otherwise fallback to resource
        if (langFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(langFile);
        } else {
            // Fallback: load from jar resource
            try (InputStream in = plugin.getResource(resourcePath)) {
                if (in != null) {
                    messages = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {
            }
        }

        // If still null, fallback to English
        if (messages == null) {
            try (InputStream in = plugin.getResource(LANG_DIR + "/messages_en.yml")) {
                if (in != null) {
                    messages = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {
            }
        }

        if (messages == null) {
            messages = new YamlConfiguration();
        }
    }

    /**
     * Gets a formatted and color-translated message by key.
     *
     * @param key  the message key (e.g. "broadcast.started")
     * @param args optional format arguments ({0}, {1}, ...)
     * @return the formatted message with color codes translated
     */
    public String getMessage(String key, Object... args) {
        String msg = messages.getString(key);
        if (msg == null) {
            return "Missing translation: " + key;
        }
        if (args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Gets the prefix + formatted message.
     */
    public String getPrefixedMessage(String key, Object... args) {
        String prefix = messages.getString("prefix", "&7[&6BetterLAN&eBroadcaster&7] ");
        return ChatColor.translateAlternateColorCodes('&', prefix) + getMessage(key, args);
    }
}
