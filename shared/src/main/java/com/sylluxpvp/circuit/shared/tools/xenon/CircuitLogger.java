package com.sylluxpvp.circuit.shared.tools.circuit;

public interface CircuitLogger {

    void log(boolean prefix, String message);
    void log(String message);

    void warn(boolean prefix, String message);
    void warn(String message);

    void error(boolean prefix, String message);
    void error(String message);

}
