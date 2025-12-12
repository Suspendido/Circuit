package com.sylluxpvp.circuit.shared.service;

import lombok.Getter;
import lombok.NonNull;
import com.sylluxpvp.circuit.shared.CircuitShared;

import java.util.Collections;
import java.util.List;

/**
 * Represents a service in Circuit.
 * <p>
 * Services are used to provide various functionalities within Circuit.
 * Each service has a unique identifier and can be enabled or disabled.
 * <p>
 * Developers, when implementing a service, you should extend this class
 * and implement the required methods to provide the desired functionality.
 * <p>
 * NOTE: Class was reused from an older project of mine, Nugget, and modified to fit Circuit's needs.
 */
public abstract class Service {

    /**
     * Indicates whether the service is enabled or not.
     * <p>
     * The getter returns the enabled/disabled state of the service.
     */
    @Getter private boolean enabled = false;

    /**
     * Returns the identifier of the service.
     * <p>This identifier is used to uniquely identify the service.
     * <p>
     * @return The identifier of the service.
     */
    @NonNull public abstract String getIdentifier();

    /**
     * Returns the service dependencies.
     * <p>This allows the service container to load services
     * <p>before this specific service is loaded.
     * <p>
     * @return The service dependencies.
     */
    public List<Class<? extends Service>> getDependencies() {
        return Collections.emptyList();
    }

    /**
     * Enables the service.
     * <p>This method is called when the service is enabled.
     * <p>
     * It should contain the logic to initialize and start the service.
     * <p>
     * When implementing this method, you should ensure that
     * all your listeners, tasks, and other resources are properly initialized here.
     */
    public abstract void enable() throws Exception;

    /**
     * Disables the service.
     * <p>This method is called when the service is disabled.
     * <p>
     * It should contain the logic to initialize and start the service.
     * <p>
     * When implementing this method, you should ensure that
     * everything that was initialized in the enable method is properly cleaned up here.
     */
    public abstract void disable() throws Exception;

    /**
     * This method is used to change the state of the service.
     * <p>
     * This can be useful in certain situations where you need to
     * change the state of the service directly.
     * <p>
     * @param e If the service should be enabled or disabled.
     */
    public void setEnabled(boolean e) {
        this.enabled = e;
        try {
            if (this.enabled) {
                this.enable();
            } else {
                this.disable();
            }
        } catch (Exception ex) {
            CircuitShared.getInstance().getLogger().error("Failed to " + (e ? "enable" : "disable") + " " + this.getIdentifier() + ": " + ex.getMessage());
            this.enabled = !e;
        }
    }

    /**
     * Toggles the enabled state of the service.
     * <p>
     * This method is used to change the state of the service.
     * It will enable the service if it is currently disabled, and disable it if it is currently enabled.
     * <p>
     * You can use this method to quickly toggle the state of your service without needing to
     * call setEnabled(true) or setEnabled(false) explicitly.
     */
    public void setEnabled() {
        this.setEnabled(!this.isEnabled());
    }

    /**
     * Logs a message to the console with the service identifier.
     * <p>
     * This method is used to log messages to the console with the service identifier.
     * It will prefix the message with the service identifier for easy identification.
     * <p>
     * @param message The message to log.
     */
    public void print(String message) {
        CircuitShared.getInstance().getLogger().log(true, "[" + this.getIdentifier() + "-service] " + message);
    }
}
