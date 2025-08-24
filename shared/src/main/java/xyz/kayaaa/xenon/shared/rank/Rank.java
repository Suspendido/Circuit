package xyz.kayaaa.xenon.shared.rank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.tools.xenon.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor @Getter
public class Rank implements Serializable {
    private final UUID UUID;
    private final String name;
    @Getter @Setter private String prefix;
    @Getter @Setter private String suffix;
    @Getter @Setter private String color;
    @Setter private List<String> permissions = new ArrayList<>();
    @Getter @Setter private int weight = 0;
    @Getter @Setter private boolean hidden = false;
    @Getter @Setter private boolean defaultRank = false;
    @Getter @Setter private boolean staff = false;
    @Getter @Setter private boolean purchasable = false;

    public List<String> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        return this.permissions;
    }

    @Override
    public String getID() {
        return this.UUID.toString();
    }

    @Override
    public String getType() {
        return "rank";
    }

    public void setPermission(String permission) {
        if (permission == null || permission.isEmpty()) return;

        if (this.permissions.contains(permission)) {
            this.permissions.remove(permission);
        } else {
            this.permissions.add(permission);
        }
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isEmpty()) return false;

        return this.permissions.contains(permission);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("uuid", this.getUUID().toString())
                .append("name", this.name)
                .append("prefix", this.prefix)
                .append("suffix", this.suffix)
                .append("permissions", this.permissions)
                .append("hidden", this.hidden)
                .append("default", this.defaultRank)
                .append("staff", this.staff)
                .append("purchasable", this.purchasable)
                .append("weight", this.weight)
                .append("color", this.color);
    }

}
