package xyz.kayaaa.xenon.bukkit.profile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.shared.profile.Profile;

@RequiredArgsConstructor @Getter
public class BukkitProfile {

    private final Profile profile;
    private final Player player;

    public void setupPlayer() {
        profile.setName(player.getName());
        PermissionAttachment attachment = player.addAttachment(XenonPlugin.getInstance());
        if (profile.getCurrentGrant() == null) {
            XenonPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant!");
            return;
        }

        if (profile.getCurrentGrant().getData() == null) {
            XenonPlugin.getInstance().getLogger().warning("Player " + player.getName() + " has no grant data!");
            return;
        }
        for (String permission : profile.getCurrentGrant().getData().getPermissions()) {
            attachment.setPermission(permission, true);
        }
        player.recalculatePermissions();
    }

}
