package com.lanmulticast;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "start", "stop", "status", "setmotd", "setdelay", "setport",
            "debug", "reload", "help", "version"
    );

    private final BetterLANBroadcaster plugin;

    public CommandHandler(BetterLANBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("betterlanbroadcaster.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.no-permission"));
            return true;
        }

        // Default: no args -> show version
        if (args.length == 0) {
            handleVersion(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStart(sender);
                break;
            case "stop":
                handleStop(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "setmotd":
                handleSetMotd(sender, args);
                break;
            case "setdelay":
                handleSetDelay(sender, args);
                break;
            case "setport":
                handleSetPort(sender, args);
                break;
            case "debug":
                handleDebug(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "help":
                handleHelp(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                        "/" + label + " <start|stop|status|setmotd|setdelay|setport|debug|reload|help|version>"));
                break;
        }
        return true;
    }

    private void handleStart(CommandSender sender) {
        MulticastBroadcaster broadcaster = plugin.getBroadcaster();
        if (broadcaster.isRunning()) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.already-running"));
        } else {
            broadcaster.start();
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.started"));
        }
    }

    private void handleStop(CommandSender sender) {
        MulticastBroadcaster broadcaster = plugin.getBroadcaster();
        if (!broadcaster.isRunning()) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.already-stopped"));
        } else {
            broadcaster.stop();
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.stopped"));
        }
    }

    private void handleStatus(CommandSender sender) {
        LanguageManager lang = plugin.getLanguageManager();
        MulticastBroadcaster broadcaster = plugin.getBroadcaster();

        String statusKey = broadcaster.isRunning() ? "broadcast.status.running" : "broadcast.status.stopped";
        sender.sendMessage(lang.getPrefixedMessage(statusKey));
        sender.sendMessage(lang.getMessage("broadcast.status.motd", broadcaster.getMotd()));
        sender.sendMessage(lang.getMessage("broadcast.status.delay", String.valueOf(broadcaster.getDelayMs())));
        sender.sendMessage(lang.getMessage("broadcast.status.port", String.valueOf(broadcaster.getPort())));

        // Show debug status
        String debugStatus = broadcaster.isDebug() ? "debug.status-on" : "debug.status-off";
        sender.sendMessage(lang.getMessage("debug.label", lang.getMessage(debugStatus)));
    }

    private void handleSetMotd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                    "/blb setmotd <MOTD>"));
            return;
        }

        // Join all remaining args as the MOTD
        String newMotd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getBroadcaster().setMotd(newMotd);
        sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.motd-set", newMotd));
    }

    private void handleSetDelay(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                    "/blb setdelay <milliseconds>"));
            return;
        }

        try {
            int ms = Integer.parseInt(args[1]);
            if (ms < 50) {
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-delay"));
                return;
            }
            plugin.getBroadcaster().setDelayMs(ms);
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.delay-set",
                    String.valueOf(ms)));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-delay"));
        }
    }

    private void handleSetPort(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                    "/blb setport <port>"));
            return;
        }

        if (args[1].equalsIgnoreCase("auto")) {
            int autoPort = plugin.getServer().getPort();
            plugin.getBroadcaster().setPort(autoPort);
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.port-set-auto",
                    String.valueOf(autoPort)));
            return;
        }

        try {
            int newPort = Integer.parseInt(args[1]);
            if (newPort < 1 || newPort > 65535) {
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-port"));
                return;
            }
            plugin.getBroadcaster().setPort(newPort);
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("broadcast.port-set",
                    String.valueOf(newPort)));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-port"));
        }
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                    "/blb debug <on|off>"));
            return;
        }

        MulticastBroadcaster broadcaster = plugin.getBroadcaster();

        switch (args[1].toLowerCase()) {
            case "on":
                broadcaster.setDebug(true);
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("debug.on"));
                break;
            case "off":
                broadcaster.setDebug(false);
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("debug.off"));
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("error.invalid-syntax",
                        "/blb debug <on|off>"));
                break;
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadPluginConfig();
        sender.sendMessage(plugin.getLanguageManager().getPrefixedMessage("config.reloaded"));
    }

    private void handleHelp(CommandSender sender) {
        LanguageManager lang = plugin.getLanguageManager();
        sender.sendMessage(lang.getMessage("help.title"));
        sender.sendMessage(lang.getMessage("help.start"));
        sender.sendMessage(lang.getMessage("help.stop"));
        sender.sendMessage(lang.getMessage("help.status"));
        sender.sendMessage(lang.getMessage("help.setmotd"));
        sender.sendMessage(lang.getMessage("help.setdelay"));
        sender.sendMessage(lang.getMessage("help.setport"));
        sender.sendMessage(lang.getMessage("help.debug"));
        sender.sendMessage(lang.getMessage("help.reload"));
        sender.sendMessage(lang.getMessage("help.help"));
        sender.sendMessage(lang.getMessage("help.version"));
        sender.sendMessage(lang.getMessage("help.footer"));
    }

    private void handleVersion(CommandSender sender) {
        LanguageManager lang = plugin.getLanguageManager();
        sender.sendMessage(lang.getMessage("version.line1", plugin.getDescription().getVersion()));
        sender.sendMessage(lang.getMessage("version.line2", plugin.getDescription().getAuthors().get(0)));
        sender.sendMessage(lang.getMessage("version.help-hint", "/blb help"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("betterlanbroadcaster.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("setmotd") && args.length == 2) {
            return Arrays.asList("<MOTD text>");
        }

        if (args[0].equalsIgnoreCase("setdelay") && args.length == 2) {
            return Arrays.asList("<milliseconds>");
        }

        if (args[0].equalsIgnoreCase("setport") && args.length == 2) {
            return Arrays.asList("<port>", "auto");
        }

        if (args[0].equalsIgnoreCase("debug") && args.length == 2) {
            return Arrays.asList("on", "off");
        }

        return new ArrayList<>();
    }
}
