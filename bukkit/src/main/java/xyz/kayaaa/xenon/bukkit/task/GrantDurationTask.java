package xyz.kayaaa.xenon.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.kayaaa.xenon.bukkit.tools.TaskUtil;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class GrantDurationTask extends BukkitRunnable {

    public GrantDurationTask() {
        TaskUtil.runTaskTimerAsynchronously(this, 0, 20);
    }

    @Override
    public void run() {
        ServiceContainer.getService(ProfileService.class).getProfiles().forEach(profile -> {
            profile.getGrants().forEach(grant -> {
                if (!grant.isExpired()) return;
                if (grant.isRemoved()) return;
                if (Bukkit.getPlayer(profile.getUUID()) != null && Bukkit.getPlayer(profile.getUUID()).isOnline()) {
                    Bukkit.getPlayer(profile.getUUID()).sendMessage(CC.translate("&cYour grant for " + (grant.getData() instanceof Rank ? ((Rank) grant.getData()).getColor() + ((Rank) grant.getData()).getName() : grant.getData().getType()) + " has expired!"));
                }
                grant.setRemoved(true);
                grant.setRemovedAt(System.currentTimeMillis());
                grant.setRemovalReason("Expired");
                grant.setRemovedBy(ServiceContainer.getService(GrantService.class).getConsole());
            });
        });
    }

}
