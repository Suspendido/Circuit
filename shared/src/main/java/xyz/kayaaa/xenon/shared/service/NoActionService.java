package xyz.kayaaa.xenon.shared.service;

import lombok.NonNull;

public abstract class NoActionService extends Service {

    @Override @NonNull
    public abstract String getIdentifier();

    @Override
    public void enable() throws Exception {
        // Since this service does not have any action, we do nothing here.
    }

    @Override
    public void disable() throws Exception {
        // Since this service does not have any action, we do nothing here as well.
    }
}
