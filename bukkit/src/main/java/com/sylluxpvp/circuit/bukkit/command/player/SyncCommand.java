package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.profile.BukkitProfile;
import com.sylluxpvp.circuit.bukkit.service.BukkitProfileService;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

@CommandAlias(value="sync")
public class SyncCommand extends BaseCommand {
    private static final int CODE_LENGTH = 8;
    private static final long CODE_EXPIRY_MS = 300000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, SyncData> PENDING_SYNCS = new ConcurrentHashMap<String, SyncData>();

    @Default
    @Description(value="Link your Minecraft account with Discord")
    public void onSync(Player player) {
        BukkitProfileService profileService = ServiceContainer.getService(BukkitProfileService.class);
        BukkitProfile bukkitProfile = profileService.find(player.getUniqueId());
        if (bukkitProfile == null) {
            player.sendMessage(CC.translate("&cError loading your profile. Please try again."));
            return;
        }
        Profile profile = bukkitProfile.getProfile();
        if (profile.getDiscordId() != null) {
            player.sendMessage(CC.translate(""));
            player.sendMessage(CC.translate("&c&lAlready Synced!"));
            player.sendMessage(CC.translate("&cYour account is already linked to Discord."));
            player.sendMessage(CC.translate("&cContact staff if you need to unlink it."));
            player.sendMessage(CC.translate(""));
            return;
        }
        String existingCode = this.findExistingCode(player.getUniqueId());
        if (existingCode != null) {
            SyncData data = PENDING_SYNCS.get(existingCode);
            if (data != null && !data.isExpired()) {
                long remainingSeconds = (data.expiryTime - System.currentTimeMillis()) / 1000L;
                player.sendMessage(CC.translate(""));
                player.sendMessage(CC.translate("&a&lSync Code"));
                player.sendMessage(CC.translate("&7You already have a pending code:"));
                player.sendMessage(CC.translate(""));
                player.sendMessage(CC.translate("&e&l" + existingCode));
                player.sendMessage(CC.translate(""));
                player.sendMessage(CC.translate("&7Use &f/sync " + existingCode + " &7in the Discord sync channel."));
                player.sendMessage(CC.translate("&7Expires in &f" + remainingSeconds + " &7seconds."));
                player.sendMessage(CC.translate(""));
                return;
            }
            PENDING_SYNCS.remove(existingCode);
        }
        String code = this.generateCode();
        SyncData syncData = new SyncData(player.getUniqueId(), player.getName(), System.currentTimeMillis() + 300000L);
        PENDING_SYNCS.put(code, syncData);
        this.storeSyncCodeInRedis(code, syncData);
        player.sendMessage(CC.translate(""));
        player.sendMessage(CC.translate("&a&lDiscord Sync"));
        player.sendMessage(CC.translate("&7Your sync code is:"));
        player.sendMessage(CC.translate(""));
        player.sendMessage(CC.translate("&e&l" + code));
        player.sendMessage(CC.translate(""));
        player.sendMessage(CC.translate("&7Go to the &f#sync &7channel in Discord"));
        player.sendMessage(CC.translate("&7and type: &f/sync " + code));
        player.sendMessage(CC.translate(""));
        player.sendMessage(CC.translate("&7This code expires in &f5 minutes&7."));
        player.sendMessage(CC.translate(""));
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 8; ++i) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        String generated = code.toString();
        if (PENDING_SYNCS.containsKey(generated)) {
            return this.generateCode();
        }
        return generated;
    }

    private String findExistingCode(UUID playerUUID) {
        for (Map.Entry<String, SyncData> entry : PENDING_SYNCS.entrySet()) {
            if (!entry.getValue().playerUUID.equals(playerUUID)) continue;
            return entry.getKey();
        }
        return null;
    }

    public static SyncData validateCode(String code) {
        SyncData data = PENDING_SYNCS.get(code.toUpperCase());
        if (data == null || data.isExpired()) {
            PENDING_SYNCS.remove(code.toUpperCase());
            return null;
        }
        return data;
    }

    public static void consumeCode(String code) {
        PENDING_SYNCS.remove(code.toUpperCase());
    }

    public static void cleanupExpiredCodes() {
        PENDING_SYNCS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void storeSyncCodeInRedis(String code, SyncData data) {
        try {
            Jedis jedis = CircuitShared.getInstance().getRedis().getJedisPublisher();
            String key = "circuit:sync:" + code;
            String json = CircuitShared.getInstance().getGson().toJson(new SyncDataJson(data.playerUUID.toString(), data.playerName, data.expiryTime));
            jedis.setex(key, 300L, json);
        } catch (Exception e) {
            CircuitPlugin.getInstance().getLogger().log(Level.SEVERE, "[SyncCommand] Failed to store sync code in Redis: " + code, e);
        }
    }

    public static class SyncData {
        public final UUID playerUUID;
        public final String playerName;
        public final long expiryTime;

        public SyncData(UUID playerUUID, String playerName, long expiryTime) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expiryTime;
        }
    }

    private static class SyncDataJson {
        public final String playerUUID;
        public final String playerName;
        public final long expiryTime;

        public SyncDataJson(String playerUUID, String playerName, long expiryTime) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.expiryTime = expiryTime;
        }
    }
}

