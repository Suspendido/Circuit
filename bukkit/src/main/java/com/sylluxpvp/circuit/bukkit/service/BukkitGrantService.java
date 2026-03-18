package com.sylluxpvp.circuit.bukkit.service;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.packets.discord.DiscordGrantUpdatePacket;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.NoActionService;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.GrantService;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

public class BukkitGrantService extends NoActionService {

    @Override
    public @NonNull String getIdentifier() {
        return "bukkit-grant";
    }

    public void removeGrant(UUID author, Profile profile, Grant<?> grant) {
        if (profile == null || grant == null) return;
        if (grant == ServiceContainer.getService(GrantService.class).getDefaultGrant() || grant.getData() instanceof Rank && ((Rank) grant.getData()).isDefaultRank()) {
            return;
        }
        if (grant.isRemoved()) {
            profile.removeGrant(grant);
            return;
        }
        grant.setRemoved(true);
        grant.setRemovedAt(System.currentTimeMillis());
        grant.setRemovedBy(author);
        ServiceContainer.getService(ProfileService.class).save(profile);
        if (profile.getDiscordId() != null && grant.getData() instanceof Rank rank) {
            DiscordGrantUpdatePacket packet = new DiscordGrantUpdatePacket(profile.getUUID(), profile.getName(), String.valueOf(profile.getDiscordId()), rank.getName(), rank.getColor(), false);
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(packet);
        }
        if (Bukkit.getPlayer(profile.getUUID()) != null && Bukkit.getPlayer(profile.getUUID()).isOnline()) {
            Bukkit.getPlayer(profile.getUUID()).sendMessage(CC.translate(grant.getData().getRemovalMessage()));
        }
    }

    public void applyGrant(UUID author, UUID target, GrantProcedure<Rank> proc) {
        CommandSender sender = author.equals(CircuitConstants.getConsoleUUID()) ? Bukkit.getConsoleSender() : Bukkit.getPlayer(author);
        GrantService service = ServiceContainer.getService(GrantService.class);
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(target);
        if (profile == null) {
            sender.sendMessage(CC.GREEN + "Couldn't find " + Bukkit.getOfflinePlayer(target).getName() + "'s profile.");
            return;
        }
        Grant<Rank> grant = service.createGrant(proc.getData(), author, proc.getDuration(), proc.getReason() == null ? "None" : proc.getReason());
        profile.addGrant(grant);
        profileService.save(profile);
        System.out.println("[Circuit-DEBUG] Checking discordId for " + profile.getName() + ": " + profile.getDiscordId());
        if (profile.getDiscordId() != null) {
            Rank rank = proc.getData();
            System.out.println("[Circuit-DEBUG] Sending DiscordGrantUpdatePacket for " + profile.getName() + " rank: " + rank.getName());
            DiscordGrantUpdatePacket packet = new DiscordGrantUpdatePacket(profile.getUUID(), profile.getName(), String.valueOf(profile.getDiscordId()), rank.getName(), rank.getColor(), true);
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(packet);
            System.out.println("[Circuit-DEBUG] Packet sent!");
        } else {
            System.out.println("[Circuit-DEBUG] No discordId, skipping packet");
        }
        sender.sendMessage(CC.GREEN + "Granted successfully!");
        if (Bukkit.getPlayer(target) == null || !Bukkit.getPlayer(target).isOnline()) return;
        Bukkit.getPlayer(target).sendMessage(CC.translate("&aYou have been granted " + proc.getData().getColor() + proc.getData().getName() + "&a!"));
    }

}
