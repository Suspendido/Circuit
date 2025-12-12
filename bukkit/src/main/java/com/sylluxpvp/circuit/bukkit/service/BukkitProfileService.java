package com.sylluxpvp.circuit.bukkit.service;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import com.sylluxpvp.circuit.bukkit.profile.BukkitProfile;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class BukkitProfileService extends Service {

    private List<BukkitProfile> profiles;

    @Override
    public @NonNull String getIdentifier() {
        return "bukkit-profile";
    }

    @Override
    public void enable() {
        this.profiles = new ArrayList<>();
    }

    @Override
    public void disable() {
        this.profiles.clear();
        this.profiles = null;
    }

    public BukkitProfile create(Profile profile) {
        if (profile == null) return null;

        BukkitProfile bukkitProfile = new BukkitProfile(profile, Bukkit.getPlayer(profile.getUUID()));
        this.profiles.add(bukkitProfile);
        return bukkitProfile;
    }

    public BukkitProfile find(Profile profile) {
        if (profile == null) return null;
        return this.profiles.stream().filter(p -> p.getProfile().getUUID().equals(profile.getUUID())).findFirst().orElse(null);
    }

    public BukkitProfile find(UUID uuid) {
        if (uuid == null) return null;
        return this.profiles.stream().filter(p -> p.getProfile().getUUID().equals(uuid)).findFirst().orElse(null);
    }
}
