package com.sylluxpvp.circuit.shared;

import lombok.Getter;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

public class CircuitConstants {

    @Getter
    private static final UUID consoleUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Getter
    private static final String playerNotFound = CC.RED + "Player not found. Please recheck their username!";

    @Getter
    private static final String playerAlreadyPunished = CC.RED + "Player is already <punishment_type>!";

    @Getter
    private static final String playerNotPunished = CC.RED + "Player is not <punishment_type>!";

}
