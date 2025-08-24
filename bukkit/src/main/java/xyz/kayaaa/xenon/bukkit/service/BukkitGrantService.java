package xyz.kayaaa.xenon.bukkit.service;

import lombok.NonNull;
import org.bukkit.Bukkit;
import xyz.kayaaa.xenon.bukkit.tools.GrantUtils;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.NoActionService;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.UUID;

public class BukkitGrantService extends NoActionService {

    @Override
    public @NonNull String getIdentifier() {
        return "bukkit-grant";
    }

    public void removeGrant(UUID author, Profile profile, Grant<Rank> grant) {
        if (profile == null || grant == null) return;
        if (grant == ServiceContainer.getService(GrantService.class).getDefaultGrant() || grant.getData().isDefaultRank()) {
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
            Bukkit.getPlayer(profile.getUUID()).sendMessage(CC.translate("&cYour grant for " + grant.getData().getColor() + grant.getData().getName() + " was removed by " + GrantUtils.getRemover(grant) + (grant.getRemovalReason() == null ? "." : " for reason: " + grant.getRemovalReason()) + "."));
        }
    }

}
