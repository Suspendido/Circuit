/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sylluxpvp.circuit.shared.service;

public interface DatabaseOperationListener {
    public void onDatabaseFailure(String var1, Exception var2);

    public void onDatabaseSuccess();
}

