package com.sylluxpvp.circuit.bukkit.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.tools.string.CC;

public class CircuitExpansion extends PlaceholderExpansion {

    public CircuitExpansion() {
    }

    @Override
    public String getIdentifier() {
        return "circuit";
    }

    @Override
    public String getAuthor() {
        return "kayaaa";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        if (profile == null) return "";

        Grant<Rank> grant = profile.getCurrentGrant();
        Rank rank = grant != null ? grant.getData() : null;

        switch (params.toLowerCase()) {
            case "rank":
            case "rank_name":
                return rank != null ? rank.getName() : "Default";

            case "rank_display":
            case "rank_displayname":
                return rank != null ? CC.translate(rank.getColor() + rank.getName()) : "Default";

            case "rank_prefix":
                return rank != null ? CC.translate(rank.getPrefix()) : "";

            case "rank_suffix":
                return rank != null ? CC.translate(rank.getSuffix()) : "";

            case "rank_color":
                return rank != null ? CC.translate(rank.getColor()) : "&7";

            case "rank_weight":
                return rank != null ? String.valueOf(rank.getWeight()) : "0";

            case "prefix":
                return rank != null ? CC.translate(rank.getPrefix()) : "";

            case "suffix":
                return rank != null ? CC.translate(rank.getSuffix()) : "";

            case "color":
            case "chatcolor":
                String color = profile.getColor();
                return color != null && !color.isEmpty() ? CC.translate(color) : "";

            case "name":
                return profile.getName();

            case "name_colored":
            case "colored_name":
                return rank != null 
                        ? CC.translate(rank.getColor() + profile.getName())
                        : profile.getName();

            case "display":
            case "displayname":
                String chatColor = profile.getColor();
                String displayColor = (chatColor != null && !chatColor.isEmpty()) 
                        ? chatColor 
                        : (rank != null ? rank.getColor() : "&7");
                return CC.translate(displayColor + profile.getName());

            case "full_display":
                String prefix = rank != null ? rank.getPrefix() : "";
                String suffix = rank != null ? rank.getSuffix() : "";
                String pColor = profile.getColor();
                String nameColor = (pColor != null && !pColor.isEmpty()) 
                        ? pColor 
                        : (rank != null ? rank.getColor() : "&7");
                return CC.translate(prefix + nameColor + profile.getName() + suffix);

            case "grants":
            case "grants_count":
                return String.valueOf(profile.getRankGrants().size());

            case "active_grants":
                return String.valueOf(profile.getRankGrants().stream().filter(Grant::isActive).count());

            case "punishments":
            case "punishments_count":
                return String.valueOf(profile.getPunishments().size());

            case "active_punishments":
                return String.valueOf(profile.getPunishments().stream().filter(Grant::isActive).count());

            case "is_staff":
                return rank != null && rank.isStaff() ? "true" : "false";

            case "is_muted":
                boolean muted = profile.getPunishments().stream()
                        .anyMatch(p -> p.isActive() && p.getData().getPunishmentType() == PunishmentType.MUTE);
                return muted ? "true" : "false";

            case "is_banned":
                boolean banned = profile.getPunishments().stream()
                        .anyMatch(p -> p.isActive() && 
                                (p.getData().getPunishmentType() == PunishmentType.BAN || 
                                 p.getData().getPunishmentType() == PunishmentType.BLACKLIST));
                return banned ? "true" : "false";

            case "chat_channel":
            case "chat-channel":
            case "channel":
                return profile.getChannel() != null ? profile.getChannel().name() : "DEFAULT";

<<<<<<< HEAD
            case "tag":
                Tag activeTag = profile.getActiveTag();
                return activeTag != null ? CC.translate(activeTag.getDisplay() + " ") : "";

            case "tag_name":
                Tag tagName = profile.getActiveTag();
                return tagName != null ? tagName.getName() : "";

            case "tag_display":
                Tag tagDisplay = profile.getActiveTag();
                return tagDisplay != null ? CC.translate(tagDisplay.getDisplay()) : "";

            case "vip":
            case "vip_icon":
                String vipIcon = profile.getVipIcon();
                return !vipIcon.isEmpty() ? CC.translate(vipIcon + " ") : "";

            case "is_vip":
            case "has_subscription":
                return profile.hasSubscription() ? "true" : "false";

=======
>>>>>>> 8bdb8ab8aade754b5669edc1af7569347551be36
            default:
                return null;
        }
    }
}
