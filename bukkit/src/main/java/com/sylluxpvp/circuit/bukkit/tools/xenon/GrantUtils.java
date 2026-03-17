package com.sylluxpvp.circuit.bukkit.tools.xenon;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.Grant;

@UtilityClass
public class GrantUtils {

    public String getAuthor(Grant grant) {
        return grant.getAuthor().equals(CircuitConstants.getConsoleUUID()) ? "&cCONSOLE" : Bukkit.getOfflinePlayer(grant.getAuthor()).getName();
    }

    public String getRemover(Grant grant) {
        return grant.getRemovedBy().equals(CircuitConstants.getConsoleUUID()) ? "&cCONSOLE" : Bukkit.getOfflinePlayer(grant.getRemovedBy()).getName();
    }

}
