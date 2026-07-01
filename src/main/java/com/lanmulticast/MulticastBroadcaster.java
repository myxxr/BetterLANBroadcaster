package com.lanmulticast;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Periodically broadcasts the server's MOTD and port via UDP multicast
 * to 224.0.2.60:4445, following the Minecraft LAN discovery protocol.
 * <p>
 * Format: [MOTD]serverMOTD[/MOTD][AD]serverPort[/AD]
 */
public class MulticastBroadcaster {

    private static final String MULTICAST_ADDRESS = "224.0.2.60";
    private static final int MULTICAST_PORT = 4445;

    private final JavaPlugin plugin;

    private String motd;
    private int port;
    private int delayMs;
    private boolean running;
    private boolean debug;
    private BukkitRunnable task;

    public MulticastBroadcaster(JavaPlugin plugin, String motd, int port, int delayMs) {
        this.plugin = plugin;
        this.motd = sanitizeMotd(motd);
        this.port = port;
        this.delayMs = Math.max(50, delayMs);
        this.running = false;
        this.debug = false;
    }

    /**
     * Starts the periodic multicast broadcast task asynchronously.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        scheduleTask();
    }

    /**
     * Stops the broadcast task.
     */
    public void stop() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @return true if currently broadcasting
     */
    public boolean isRunning() {
        return running;
    }

    public void setMotd(String newMotd) {
        this.motd = sanitizeMotd(newMotd);
        if (running) {
            reschedule();
        }
    }

    public String getMotd() {
        return motd;
    }

    public void setDelayMs(int newDelayMs) {
        this.delayMs = Math.max(50, newDelayMs);
        if (running) {
            reschedule();
        }
    }

    public int getDelayMs() {
        return delayMs;
    }

    /**
     * Updates the broadcast port.
     */
    public void setPort(int newPort) {
        if (newPort > 0 && newPort <= 65535) {
            this.port = newPort;
        }
    }

    public int getPort() {
        return port;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * Cancels the current task and schedules a new one.
     */
    private void reschedule() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        scheduleTask();
    }

    /**
     * Schedules a repeating async task. Converts ms to ticks (1 tick = 50ms).
     */
    private void scheduleTask() {
        long ticks = Math.max(1, delayMs / 50L);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }
                sendBroadcast();
            }
        };

        task.runTaskTimerAsynchronously(plugin, 0L, ticks);
    }

    /**
     * Builds and sends the multicast packet.
     */
    private void sendBroadcast() {
        String resolvedMotd = resolveMotd();
        String payload = "[MOTD]" + resolvedMotd + "[/MOTD][AD]" + port + "[/AD]";
        byte[] message = payload.getBytes(StandardCharsets.UTF_8);

        if (debug) {
            plugin.getLogger().info("[DEBUG] Sending broadcast: " + payload
                    + " -> " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT);
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(message, message.length, group, MULTICAST_PORT);
            socket.send(packet);

            if (debug) {
                plugin.getLogger().info("[DEBUG] Broadcast sent successfully (" + message.length + " bytes)");
            }
        } catch (UnknownHostException e) {
            plugin.getLogger().warning("Invalid multicast address: " + MULTICAST_ADDRESS);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send multicast broadcast: " + e.getMessage());
        }
    }

    /**
     * Replaces placeholders in the MOTD template with real-time server values.
     * <ul>
     *   <li>{@code {online}} — current online player count</li>
     *   <li>{@code {max}} — maximum player count</li>
     * </ul>
     */
    private String resolveMotd() {
        String resolved = motd;
        int online = plugin.getServer().getOnlinePlayers().size();
        int max = plugin.getServer().getMaxPlayers();
        resolved = resolved.replace("{online}", String.valueOf(online));
        resolved = resolved.replace("{max}", String.valueOf(max));
        return resolved;
    }

    /**
     * Sanitizes the MOTD to remove characters that could break the protocol format.
     */
    private String sanitizeMotd(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("[/MOTD]", "")
                  .replace("[/AD]", "")
                  .replace("[MOTD]", "")
                  .replace("[AD]", "");
    }
}
