package com.sylluxpvp.circuit.bukkit.api;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * CircuitAPI provides a simple interface for other plugins to access Circuit's functionality.
 */
public class CircuitAPI {

    @Getter
    private static CircuitAPI instance;

    public CircuitAPI() {
        instance = this;
    }

    /**
     * Gets a player's profile by their UUID.
     *
     * @param uuid The UUID of the player
     * @return The player's Profile, or null if not found
     */
    public Profile getProfile(UUID uuid) {
        if (uuid == null) return null;
        return ServiceContainer.getService(ProfileService.class).find(uuid);
    }

    /**
     * Gets a player's profile.
     *
     * @param player The player
     * @return The player's Profile, or null if not found
     */
    public Profile getProfile(Player player) {
        if (player == null) return null;
        return getProfile(player.getUniqueId());
    }

    /**
     * Gets the current/active rank of a player.
     *
     * @param uuid The UUID of the player
     * @return The player's current Rank, or null if not found
     */
    public Rank getCurrentRank(UUID uuid) {
        Profile profile = getProfile(uuid);
        if (profile == null) return null;
        Grant<Rank> grant = profile.getCurrentGrant();
        return grant != null ? grant.getData() : null;
    }

    /**
     * Gets the current/active rank of a player.
     *
     * @param player The player
     * @return The player's current Rank, or null if not found
     */
    public Rank getCurrentRank(Player player) {
        if (player == null) return null;
        return getCurrentRank(player.getUniqueId());
    }

    /**
     * Gets the rank name of a player.
     *
     * @param player The player
     * @return The rank name, or "Default" if not found
     */
    public String getRankName(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null ? rank.getName() : "Default";
    }

    /**
     * Gets the rank prefix of a player.
     *
     * @param player The player
     * @return The rank prefix, or empty string if not found
     */
    public String getRankPrefix(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null && rank.getPrefix() != null ? rank.getPrefix() : "";
    }

    /**
     * Gets the rank suffix of a player.
     *
     * @param player The player
     * @return The rank suffix, or empty string if not found
     */
    public String getRankSuffix(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null && rank.getSuffix() != null ? rank.getSuffix() : "";
    }

    /**
     * Gets the rank color of a player.
     *
     * @param player The player
     * @return The rank color code, or "&7" if not found
     */
    public String getRankColor(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null && rank.getColor() != null ? rank.getColor() : "&7";
    }

    /**
     * Gets the rank weight of a player.
     *
     * @param player The player
     * @return The rank weight, or 0 if not found
     */
    public int getRankWeight(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null ? rank.getWeight() : 0;
    }

    /**
     * Checks if a player is staff.
     *
     * @param player The player
     * @return true if the player's rank is marked as staff, false otherwise
     */
    public boolean isStaff(Player player) {
        Rank rank = getCurrentRank(player);
        return rank != null && rank.isStaff();
    }

    /**
     * Gets the custom chat color of a player if set.
     *
     * @param player The player
     * @return The custom color, or null if not set
     */
    public String getCustomColor(Player player) {
        Profile profile = getProfile(player);
        if (profile == null) return null;
        String color = profile.getColor();
        return (color != null && !color.isEmpty()) ? color : null;
    }

    /**
     * Gets the display color for a player (custom color if set, otherwise rank color).
     *
     * @param player The player
     * @return The display color code
     */
    public String getDisplayColor(Player player) {
        String customColor = getCustomColor(player);
        if (customColor != null) return customColor;
        return getRankColor(player);
    }

    /**
     * Gets the highest visible rank of a player (non-hidden rank with highest weight).
     *
     * @param player The player
     * @return The highest visible Rank, or null if not found
     */
    public Rank getHighestVisibleRank(Player player) {
        Profile profile = getProfile(player);
        if (profile == null) return null;
        
        return profile.getRankGrants().stream()
                .filter(grant -> grant.isActive() && grant.getData() != null && !grant.getData().isHidden())
                .map(Grant::getData)
                .max((r1, r2) -> Integer.compare(r1.getWeight(), r2.getWeight()))
                .orElse(null);
    }

    /**
     * Gets the highest rank of a player (regardless of visibility).
     *
     * @param player The player
     * @return The highest Rank, or null if not found
     */
    public Rank getHighestRank(Player player) {
        Profile profile = getProfile(player);
        if (profile == null) return null;
        Grant<Rank> grant = profile.getCurrentGrant();
        return grant != null ? grant.getData() : null;
    }
}
