package xyz.kayaaa.xenon.bukkit.service;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.grant.GrantProcedure;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.NoActionService;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

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
        if (Bukkit.getPlayer(profile.getUUID()) != null && Bukkit.getPlayer(profile.getUUID()).isOnline()) {
            Bukkit.getPlayer(profile.getUUID()).sendMessage(CC.translate(grant.getData().getRemovalMessage()));
        }
    }

    public void applyGrant(UUID author, UUID target, GrantProcedure<Rank> proc) {
        CommandSender sender = author.equals(XenonConstants.getConsoleUUID()) ? Bukkit.getConsoleSender() : Bukkit.getPlayer(author);
        GrantService service = ServiceContainer.getService(GrantService.class);
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        if (profile == null) {
            sender.sendMessage(CC.GREEN + "Couldn't find " + Bukkit.getOfflinePlayer(target).getName() + "'s profile.");
            return;
        }
        Grant<Rank> grant = service.createGrant(proc.getData(), author, proc.getDuration(), proc.getReason() == null ? "None" : proc.getReason());
        profile.addGrant(grant);
        sender.sendMessage(CC.GREEN + "Granted successfully!");
        if (Bukkit.getPlayer(target) == null || !Bukkit.getPlayer(target).isOnline()) return;
        Bukkit.getPlayer(target).sendMessage(CC.translate("&aYou have been granted " + proc.getData().getColor() + proc.getData().getName() + "&a!"));
    }

}
