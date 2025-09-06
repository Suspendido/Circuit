package xyz.kayaaa.xenon.bukkit.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ConfigUtil;
import xyz.kayaaa.xenon.shared.service.Service;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;

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
        this.filterEnabled = XenonPlugin.getInstance().getFilterConfig().getBoolean("enabled");
        if (!filterEnabled) {
            this.filteredWords = new HashSet<>();
            return;
        }
        this.filteredWords = new HashSet<>(XenonPlugin.getInstance().getFilterConfig().getStringList("filtered-words"));
    }

    @Override
    public void disable() {
        if (!this.filterEnabled) return;

        XenonPlugin.getInstance().getFilterConfig().set("enabled", true);
        XenonPlugin.getInstance().getFilterConfig().set("filtered-words", new ArrayList<>(filteredWords));
        ConfigUtil.saveConfig(new File(XenonPlugin.getInstance().getDataFolder(), "modules"), XenonPlugin.getInstance().getFilterConfig());
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
        if (player.hasPermission("xenon.chat.bypassfilter")) return false;

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
