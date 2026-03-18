package com.sylluxpvp.circuit.shared.rank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor @Getter @Setter
public class Rank implements Serializable {
    private final UUID uuid;
    private final String name;
    private String prefix;
    private String suffix;
    private String color;
    private List<String> permissions = new ArrayList<>();
    private List<String> inheritances = new ArrayList<>();
    private int weight = 0;
    private boolean hidden = false;
    private boolean defaultRank = false;
    private boolean staff = false;
    private boolean purchasable = false;
    private String discordRoleId = null;

    public List<String> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        return this.permissions;
    }

    public List<String> getInheritances() {
        if (inheritances == null) {
            inheritances = new ArrayList<>();
        }
        return this.inheritances;
    }

    public void addInheritance(String rankName) {
        if (rankName == null || rankName.isEmpty()) return;
        if (!this.getInheritances().contains(rankName)) {
            this.getInheritances().add(rankName);
        }
    }

    public void removeInheritance(String rankName) {
        if (rankName == null || rankName.isEmpty()) return;
        this.getInheritances().remove(rankName);
    }

    @Override
    public String getID() {
        return this.uuid.toString();
    }

    @Override
    public String getType() {
        return "rank";
    }

    @Override
    public String getExpiryMessage() {
        return "&aTu rango " + color + name + " &aa expirado!";
    }

    @Override
    public String getRemovalMessage() {
        return "&aTu rango " + color + name + " &aa sido removido!";
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
                .append("uuid", this.uuid.toString())
                .append("name", this.name)
                .append("prefix", this.prefix)
                .append("suffix", this.suffix)
                .append("permissions", this.permissions)
                .append("hidden", this.hidden)
                .append("default", this.defaultRank)
                .append("staff", this.staff)
                .append("purchasable", this.purchasable)
                .append("weight", this.weight)
                .append("color", this.color)
                .append("inheritances", this.inheritances)
                .append("discordRoleId", this.discordRoleId);
    }

}
