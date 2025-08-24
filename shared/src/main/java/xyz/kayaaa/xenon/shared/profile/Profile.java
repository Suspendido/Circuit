package xyz.kayaaa.xenon.shared.profile;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class Profile {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final UUID UUID;
    @Setter private String name;
    @Setter private String color;

    private final String token;
    private final List<String> permissions;
    private final List<Grant<?>> grants;

    public Profile(UUID UUID) {
        this.UUID = Objects.requireNonNull(UUID, "UUID cannot be null");
        this.name = null;
        this.color = null;
        this.token = generateToken();
        this.permissions = new ArrayList<>();
        this.grants = new ArrayList<>();
    }

    public Profile(UUID UUID, String name, String color, String token, List<String> permissions, List<Grant<?>> grants) {
        this.UUID = UUID;
        this.name = name;
        this.color = color;
        this.token = token;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.grants = grants != null ? grants : new ArrayList<>();
    }

    public Grant<Rank> getCurrentGrant() {
        return grants.stream()
                .filter(grant -> grant.isActive() && grant.getData() instanceof Rank)
                .map(grant -> (Grant<Rank>) grant)
                .max(Comparator.comparingInt(grant -> grant.getData().getWeight()))
                .orElse(ServiceContainer.getService(GrantService.class).getDefaultGrant());
    }

    private String generateToken() {
        return ThreadLocalRandom.current().ints(16, 0, TOKEN_CHARS.length())
                .mapToObj(TOKEN_CHARS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public void addGrant(Grant<?> grant) {
        if (grant == null || this.grants.contains(grant)) return;
        this.grants.add(grant);
    }

    public void removeGrant(Grant<?> grant) {
        if (grant == null || !this.grants.contains(grant)) return;
        this.grants.remove(grant);
    }

    public Grant<Rank> findByRank(Rank rank) {
        if (rank == null) return null;

        return (Grant<Rank>) this.grants.stream().filter(grant -> grant.getData() instanceof Rank && grant.getData().equals(rank)).findFirst().orElse(null);
    }

    public boolean hasGrant(Grant<?> grant) {
        if (grant == null) return false;

        return this.grants.contains(grant);
    }

    public boolean hasRank(Rank rank) {
        if (rank == null) return false;

        return this.grants.stream().anyMatch(grant -> grant.getData() instanceof Rank && grant.getData().equals(rank));
    }

    public Document toDocument() {
        Document doc = new Document()
                .append("uuid", this.UUID.toString())
                .append("name", this.name)
                .append("color", this.color)
                .append("token", this.token)
                .append("permissions", this.permissions)
                .append("lastUpdated", new Date());

        List<Document> grantsDocuments = new ArrayList<>();
        for (Grant<?> grant : this.grants) {
            grantsDocuments.add(grant.toDocument());
        }
        doc.append("grants", grantsDocuments);

        return doc;
    }
}
