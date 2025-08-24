package xyz.kayaaa.xenon.bukkit.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GrantService;

@UtilityClass
public class GrantUtils {

    public String getAuthor(Grant grant) {
        return grant.getAuthor().equals(ServiceContainer.getService(GrantService.class).getConsole()) ? "&cCONSOLE" : Bukkit.getOfflinePlayer(grant.getAuthor()).getName();
    }

    public String getRemover(Grant grant) {
        return grant.getRemovedBy().equals(ServiceContainer.getService(GrantService.class).getConsole()) ? "&cCONSOLE" : Bukkit.getOfflinePlayer(grant.getRemovedBy()).getName();
    }

}
