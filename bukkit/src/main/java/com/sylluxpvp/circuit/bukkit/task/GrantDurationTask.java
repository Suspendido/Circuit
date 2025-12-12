package com.sylluxpvp.circuit.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

public class GrantDurationTask extends BukkitRunnable {

    public GrantDurationTask() {
        TaskUtil.runTaskTimerAsynchronously(this, 0, 20);
    }

    @Override
    public void run() {
        ServiceContainer.getService(ProfileService.class).getOnlineProfiles().forEach(profile -> {
            profile.getAllGrants().forEach(grant -> {
                if (!grant.isExpired()) return;
                if (grant.isRemoved()) return;
                if (Bukkit.getPlayer(profile.getUUID()) != null && Bukkit.getPlayer(profile.getUUID()).isOnline()) {
                    Bukkit.getPlayer(profile.getUUID()).sendMessage(CC.translate(grant.getData().getExpiryMessage()));
                }
                grant.setRemoved(true);
                grant.setRemovedAt(System.currentTimeMillis());
                grant.setRemovalReason("Expired");
                grant.setRemovedBy(CircuitConstants.getConsoleUUID());
            });
        });
    }

}
