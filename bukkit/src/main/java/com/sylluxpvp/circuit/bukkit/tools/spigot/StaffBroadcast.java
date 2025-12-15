package com.sylluxpvp.circuit.bukkit.tools.spigot;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.BroadcastPacket;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.ManagementBroadcastPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StaffBroadcast {

<<<<<<< HEAD
    private static final String PREFIX = "&5[SC] &d[%server%] ";
=======
    private static final String PREFIX = "&5[SC]&d[%server%] ";
>>>>>>> 8bdb8ab8aade754b5669edc1af7569347551be36

    /**
     * Send a local broadcast to players with a specific permission (current server only)
     */
    public static void broadcastLocal(String message, String permission) {
        String serverName = getServerName();
        String formatted = CC.translate(PREFIX.replace("%server%", serverName) + message);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(formatted);
            }
        }
    }

    /**
     * Send a broadcast to all servers via Redis
     */
    public static void broadcastGlobal(String message) {
        String serverName = getServerName();
        String formatted = PREFIX.replace("%server%", serverName) + message;
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new BroadcastPacket(serverName, formatted));
    }

    /**
     * Send a management broadcast to all servers via Redis (only to players with permission)
     */
    public static void broadcastManagement(String message, String permission) {
        String serverName = getServerName();
        String formatted = PREFIX.replace("%server%", serverName) + message;
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new ManagementBroadcastPacket(serverName, formatted, permission));
    }

    /**
     * Send a staff broadcast to all servers (circuit.staff permission)
     */
    public static void broadcastStaff(String message) {
        broadcastManagement(message, "circuit.staff");
    }

    /**
     * Send an admin broadcast to all servers (circuit.admin permission)
     */
    public static void broadcastAdmin(String message) {
        broadcastManagement(message, "circuit.admin");
    }

    /**
     * Get the display name of a player with their rank color
     */
    public static String getDisplayName(Player player) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        String color = "&f";
        if (profile != null && profile.getCurrentGrant() != null && profile.getCurrentGrant().getData() != null) {
            color = profile.getCurrentGrant().getData().getColor();
        }
        return color + player.getName();
    }

    private static String getServerName() {
        return CircuitPlugin.getInstance().getMainConfig().getString("server.name", "Unknown");
    }
}
