package com.sylluxpvp.circuit.bukkit.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ConfigUtil;
import com.sylluxpvp.circuit.shared.service.Service;
import com.sylluxpvp.circuit.shared.tools.string.StringHelper;

import java.io.File;
import java.util.*;

@Getter @Setter
public class BukkitChatService extends Service {

    private boolean chatEnabled = true;
    private long slowdown = 3000;

    private boolean filterEnabled = false;
    private Set<String> filteredWords;

    private final Map<Player, Long> chatCooldown = new HashMap<>();

    @Override @NonNull
    public String getIdentifier() {
        return "bukkit-chat";
    }

    @Override
    public void enable() {
        this.filterEnabled = CircuitPlugin.getInstance().getFilterConfig().getBoolean("enabled");
        if (!filterEnabled) {
            this.filteredWords = new HashSet<>();
            return;
        }
        this.filteredWords = new HashSet<>(CircuitPlugin.getInstance().getFilterConfig().getStringList("filtered-words"));
    }

    @Override
    public void disable() {
        if (!this.filterEnabled) return;

        CircuitPlugin.getInstance().getFilterConfig().set("enabled", true);
        CircuitPlugin.getInstance().getFilterConfig().set("filtered-words", new ArrayList<>(filteredWords));
        ConfigUtil.saveConfig(new File(CircuitPlugin.getInstance().getDataFolder(), "modules"), CircuitPlugin.getInstance().getFilterConfig());
        this.filterEnabled = false;
        this.filteredWords.clear();
        this.filteredWords = null;
    }

    public boolean canChat(Player player) {
        if (!chatEnabled) return false;
        if (!chatCooldown.containsKey(player)) return true;

        return System.currentTimeMillis() - chatCooldown.get(player) > slowdown;
    }

    public boolean shouldFilter(Player player, String message) {
        if (!filterEnabled) return false;
        if (player.hasPermission("circuit.chat.bypassfilter")) return false;

        return filteredWords.stream().anyMatch(word -> StringHelper.clean(message).contains(word));
    }

    public void filterWord(String word) {
        if (!this.filterEnabled) return;
        if (!this.filteredWords.add(word)) this.filteredWords.remove(word);
    }

    public boolean isFiltered(String word) {
        if (!this.filterEnabled) return false;
        return this.filteredWords.contains(word);
    }

    public void toggleChat() {
        this.chatEnabled = !this.chatEnabled;
    }

    public void toggleFilter() {
        this.filterEnabled = !this.filterEnabled;
    }
}
