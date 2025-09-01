package xyz.kayaaa.xenon.bukkit.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.shared.service.NoActionService;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class BukkitChatService extends NoActionService {

    private boolean chatEnabled = true;
    private long slowdown = 3000;
    private final Map<Player, Long> chatCooldown = new HashMap<>();

    @Override @NonNull
    public String getIdentifier() {
        return "bukkit-chat";
    }

    public boolean canChat(Player player) {
        if (!chatEnabled) return false;
        if (!chatCooldown.containsKey(player)) return true;

        return System.currentTimeMillis() - chatCooldown.get(player) > slowdown;
    }

    public void toggleChat() {
        this.chatEnabled = !this.chatEnabled;
    }
}
