package com.sylluxpvp.circuit.shared.server;

import org.apache.commons.lang3.StringUtils;

public enum ServerType {
    DEFAULT,
    HUB,
    PRACTICE,
    SOUP;

    public String prettyName() {
        return StringUtils.capitalize(this.name().toLowerCase());
    }
}
