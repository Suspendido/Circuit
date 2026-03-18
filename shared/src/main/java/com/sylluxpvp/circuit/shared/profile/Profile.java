package com.sylluxpvp.circuit.shared.profile;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.chat.ChatChannel;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.GrantService;
import com.sylluxpvp.circuit.shared.service.impl.TagService;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.tools.java.CryptographyUtils;
import com.sylluxpvp.circuit.shared.tools.string.StringHelper;

import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class Profile {

    private final UUID UUID;
    private final String token;
    private final List<String> permissions;
    private final List<Grant<Rank>> rankGrants;
    private final List<Grant<Punishment>> punishments;
    private String address;
    private String name;
    private String color;
    private ChatChannel channel = ChatChannel.DEFAULT;
    private int coins;
    private UUID activeTagId = null;
    private boolean vipStatus = false;
    private Long discordId = null;

    public Profile(UUID UUID) {
        this.UUID = Objects.requireNonNull(UUID, "UUID cannot be null");
        this.address = null;
        this.name = null;
        this.color = "";
        this.token = StringHelper.generateString(16);
        this.permissions = new ArrayList<>();
        this.rankGrants = new ArrayList<>();
        this.punishments = new ArrayList<>();
        this.coins = 0;
    }

    public Profile(String address, UUID UUID, String name, String color, String token, List<String> permissions, List<Grant<Rank>> rankGrants, List<Grant<Punishment>> punishments, int coins, UUID activeTagId, boolean vipStatus, Long discordId) {
        this.UUID = UUID;
        this.address = address;
        this.name = name;
        this.color = color;
        this.token = token;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.rankGrants = rankGrants != null ? rankGrants : new ArrayList<>();
        this.punishments = punishments != null ? punishments : new ArrayList<>();
        this.coins = coins;
        this.activeTagId = activeTagId;
        this.vipStatus = vipStatus;
        this.discordId = discordId;
    }

    public Tag getActiveTag() {
        if (activeTagId == null) return null;
        return ServiceContainer.getService(TagService.class).getTag(activeTagId);
    }

    public Grant<Rank> getCurrentGrant() {
        return rankGrants.stream().filter(grant -> grant.isActive() && grant.getData() != null).max(Comparator.comparingInt(grant -> grant.getData().getWeight())).orElse(ServiceContainer.getService(GrantService.class).getDefaultGrant());
    }

    public void addGrant(Grant<?> grant) {
        Validate.notNull(grant, "Grant cannot be null");
        Validate.isTrue(grant.getData() != null, "Grant data cannot be null");
        Validate.isTrue(grant.getData() instanceof Rank || grant.getData() instanceof Punishment, "Grant data must be a Rank or a Punishment");

        if (grant.getData() instanceof Rank) {
            Validate.isTrue(!this.rankGrants.contains(grant), "Rank grant already exists");
            this.rankGrants.add((Grant<Rank>) grant);
            CircuitShared.getInstance().getLogger().log("Added a rank to " + this.UUID.toString() + " with ID " + grant.getUuid());
        }

        if (grant.getData() instanceof Punishment) {
            Validate.isTrue(!this.punishments.contains(grant), "Punishment already exists");
            this.punishments.add((Grant<Punishment>) grant);
            CircuitShared.getInstance().getLogger().log("Added a punishment to " + this.UUID.toString() + " with ID " + grant.getUuid());
        }
    }

    public void removeGrant(Grant<?> grant) {
        Validate.notNull(grant, "Grant cannot be null");
        Validate.isTrue(grant.getData() != null, "Grant data cannot be null");
        Validate.isTrue(grant.getData() instanceof Rank || grant.getData() instanceof Punishment, "Grant data must be a Rank or a Punishment");

        if (grant.getData() instanceof Rank) {
            Validate.isTrue(this.rankGrants.contains(grant), "Rank grant doesn't exist");
            this.rankGrants.remove(grant);
        } else {
            Validate.isTrue(this.punishments.contains(grant), "Punishment doesn't exist");
            this.punishments.remove(grant);
        }

        CircuitShared.getInstance().getLogger().log("Removed a grant from " + this.UUID.toString() + " with ID " + grant.getUuid());
    }

    public Grant<Rank> findByRank(Rank rank) {
        Validate.notNull(rank, "Rank cannot be null");

        return this.rankGrants.stream().filter(grant -> grant.getData() != null && grant.getData().equals(rank)).findFirst().orElse(null);
    }

    public List<Grant<Punishment>> getAllPunishmentsByType(PunishmentType type) {
        Validate.notNull(type, "Type cannot be null");

        return this.punishments.stream().filter(grant -> grant.getData() != null && grant.getData().getPunishmentType().equals(type)).collect(Collectors.toList());
    }

    public Grant<Punishment> findPunishmentByType(PunishmentType type) {
        Validate.notNull(type, "Type cannot be null");

        return this.punishments.stream().filter(grant -> grant.getData() != null && grant.getData().getPunishmentType().equals(type)).findFirst().orElse(null);
    }

    public Grant<Punishment> findActivePunishment(PunishmentType type) {
        Validate.notNull(type, "Type cannot be null");
        return this.punishments.stream().filter(grant -> grant.getData() != null && grant.getData().getPunishmentType().equals(type) && grant.isActive()).findFirst().orElse(null);
    }

    public Grant<Punishment> findActivePunishment() {
        Comparator<Grant<Punishment>> comparator = Comparator.comparingInt(grant -> grant.getData().getPunishmentType().getPriority());
        return this.punishments.stream().filter(grant -> grant.getData() != null && grant.isActive() && grant.getData().getPunishmentType() != PunishmentType.KICK).max(comparator).orElse(null);
    }

    public boolean hasGrant(Grant<?> grant) {
        Validate.notNull(grant, "Grant cannot be null");
        Validate.isTrue(grant.getData() != null, "Grant data cannot be null");
        Validate.isTrue(grant.getData() instanceof Rank || grant.getData() instanceof Punishment, "Grant data must be a Rank or a Punishment");

        if (grant.getData() instanceof Rank) {
            return this.rankGrants.contains(grant);
        } else {
            return this.punishments.contains(grant);
        }
    }

    public List<Grant<?>> getAllGrants() {
        List<Grant<?>> grants = new ArrayList<>();
        grants.addAll(rankGrants);
        grants.addAll(punishments);
        return grants;
    }

    public boolean hasRank(Rank rank) {
        Validate.notNull(rank, "Rank cannot be null");

        return rankGrants.stream().anyMatch(grant -> grant.getData() != null && grant.getData().equals(rank) && grant.isActive());
    }

    public boolean hasSubscription() {
        return vipStatus;
    }

    public String getVipIcon() {
        return vipStatus ? "&6✪" : "";
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public void togglePermission(String permission) {
        if (permissions.contains(permission)) {
            permissions.remove(permission);
        } else {
            permissions.add(permission);
        }
    }

    public Document toDocument() {
        Document doc = new Document()
                .append("address", CryptographyUtils.encrypt(address, token))
                .append("uuid", UUID.toString())
                .append("name", name)
                .append("color", color)
                .append("token", token)
                .append("permissions", permissions)
                .append("coins", coins)
                .append("activeTagId", activeTagId != null ? activeTagId.toString() : null)
                .append("vipStatus", vipStatus)
                .append("lastUpdated", new Date());

        List<Document> ranks = new ArrayList<>();
        rankGrants.forEach(grant -> ranks.add(grant.toDocument()));
        doc.append("rankGrants", ranks);

        List<Document> punishments = new ArrayList<>();
        this.punishments.forEach(grant -> punishments.add(grant.toDocument()));
        doc.append("punishments", punishments);

        return doc;
    }
}
