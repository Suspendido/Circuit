package xyz.kayaaa.xenon.shared.service;

import lombok.Getter;
import lombok.NonNull;
import xyz.kayaaa.xenon.shared.XenonShared;

/**
 * Represents a service in Xenon.
 * <p>
 * Services are used to provide various functionalities within Xenon.
 * Each service has a unique identifier and can be enabled or disabled.
 * <p>
 * Developers, when implementing a service, you should extend this class
 * and implement the required methods to provide the desired functionality.
 * <p>
 * NOTE: Class was reused from an older project of mine, Nugget, and modified to fit Xenon's needs.
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
                this.print("&a" + this.getIdentifier() + "-service has been enabled.");
            } else {
                this.disable();
                this.print("&c" + this.getIdentifier() + "-service has been disabled.");
            }
        } catch (Exception ex) {
            this.print("&cAn error occurred while trying to " + (e ? "enable" : "disable") + " the service: " + ex.getMessage());
            ex.printStackTrace();
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
        XenonShared.getInstance().getLogger().log(true, "[" + this.getIdentifier() + "-service] " + message);
    }
}
