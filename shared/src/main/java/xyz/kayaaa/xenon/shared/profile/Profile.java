package xyz.kayaaa.xenon.shared.profile;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.chat.ChatChannel;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.punishment.Punishment;
import xyz.kayaaa.xenon.shared.punishment.PunishmentType;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;
import xyz.kayaaa.xenon.shared.tools.java.CryptographyUtils;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Profile {

    private final UUID UUID;
    private final String token;
    private final List<String> permissions;
    private final List<Grant<Rank>> rankGrants;
    private final List<Grant<Punishment>> punishments;
    @Setter
    private String address;
    @Setter
    private String name;
    @Setter
    private String color;
    @Setter
    private ChatChannel channel = ChatChannel.DEFAULT;

    public Profile(UUID UUID) {
        this.UUID = Objects.requireNonNull(UUID, "UUID cannot be null");
        this.address = null;
        this.name = null;
        this.color = "";
        this.token = StringHelper.generateString(16);
        this.permissions = new ArrayList<>();
        this.rankGrants = new ArrayList<>();
        this.punishments = new ArrayList<>();
    }

    public Profile(String address, UUID UUID, String name, String color, String token, List<String> permissions, List<Grant<Rank>> rankGrants, List<Grant<Punishment>> punishments) {
        this.UUID = UUID;
        this.address = address;
        this.name = name;
        this.color = color;
        this.token = token;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.rankGrants = rankGrants != null ? rankGrants : new ArrayList<>();
        this.punishments = punishments != null ? punishments : new ArrayList<>();
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
            XenonShared.getInstance().getLogger().log("Added a rank to " + this.UUID.toString() + " with ID " + grant.getUUID());
        }

        if (grant.getData() instanceof Punishment) {
            Validate.isTrue(!this.punishments.contains(grant), "Punishment already exists");
            this.punishments.add((Grant<Punishment>) grant);
            XenonShared.getInstance().getLogger().log("Added a punishment to " + this.UUID.toString() + " with ID " + grant.getUUID());
        }
    }

    public void removeGrant(Grant<?> grant) {
        Validate.notNull(grant, "Grant cannot be null");
        Validate.isTrue(grant.getData() != null, "Grant data cannot be null");
        Validate.isTrue(grant.getData() instanceof Rank || grant.getData() instanceof Punishment, "Grant data must be a Rank or a Punishment");

        if (grant.getData() instanceof Rank) {
            Validate.isTrue(this.rankGrants.contains(grant), "Rank grant doesn't exist");
            this.rankGrants.remove(grant);
        } else if (grant.getData() instanceof Punishment) {
            Validate.isTrue(this.punishments.contains(grant), "Punishment doesn't exist");
            this.punishments.remove(grant);
        }

        XenonShared.getInstance().getLogger().log("Removed a grant from " + this.UUID.toString() + " with ID " + grant.getUUID());
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
        } else if (grant.getData() instanceof Punishment) {
            return this.punishments.contains(grant);
        }
        return false;
    }

    public List<Grant<?>> getAllGrants() {
        List<Grant<?>> grants = new ArrayList<>();
        grants.addAll(this.rankGrants);
        grants.addAll(this.punishments);
        return grants;
    }

    public boolean hasRank(Rank rank) {
        Validate.notNull(rank, "Rank cannot be null");

        return this.rankGrants.stream().anyMatch(grant -> grant.getData() != null && grant.getData().equals(rank) && grant.isActive());
    }

    public Document toDocument() {
        Document doc = new Document().append("address", CryptographyUtils.encrypt(this.address, this.token)).append("uuid", this.UUID.toString()).append("name", this.name).append("color", this.color).append("token", this.token).append("permissions", this.permissions).append("lastUpdated", new Date());

        List<Document> ranks = new ArrayList<>();
        this.rankGrants.forEach(grant -> ranks.add(grant.toDocument()));
        doc.append("rankGrants", ranks);

        List<Document> punishments = new ArrayList<>();
        this.punishments.forEach(grant -> punishments.add(grant.toDocument()));
        doc.append("punishments", punishments);

        return doc;
    }
}
