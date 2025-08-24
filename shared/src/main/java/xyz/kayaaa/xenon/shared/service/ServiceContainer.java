package xyz.kayaaa.xenon.shared.service;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.tools.java.ClassUtils;
import xyz.kayaaa.xenon.shared.tools.java.Validation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceContainer is a utility class that manages the registration and retrieval of services inside Xenon.
 * It allows services to be registered and later retrieved by their class type.
 * <p>
 * NOTE: Class was reused from an older project of mine, Nugget, and modified to fit Xenon's needs.
 */
@UtilityClass
public class ServiceContainer {

    static {
        services = new ConcurrentHashMap<>();
        registerServices();
    }

    public void loadClass() {
        // This method is used to ensure that the static block is executed
        // and services are registered when the class is loaded.
    }

    /**
     * A map that holds registered services, keyed by their class type.
     */
    private static final Map<Class<? extends Service>, Service> services;

    private Service newServiceInstance(Class<?> clazz) {
        Validation.notNull(clazz, "Class cannot be null");
        Validation.isTrue(Service.class.isAssignableFrom(clazz), "Class " + clazz.getName() + " is not a Repository.");

        try {
            return (Service) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create service instance for " + clazz.getName(), e);
        }
    }

    @SneakyThrows
    private void registerServices() {
        List<Class<?>> classes = ClassUtils.getClasses(XenonShared.getInstance().getFile(), Service.class.getPackage().getName() + ".impl");
        XenonShared.getInstance().getLogger().log("Found " + classes.size() + " services.");

        classes.stream()
                .filter(c -> !c.getName().contains("$"))
                .map(ServiceContainer::newServiceInstance)
                .forEach(ServiceContainer::registerService);
    }

    /**
     * Registers a service in the service container.
     *
     * @param service the service to register
     * @throws IllegalArgumentException if the service is null
     */
    public void registerService(Service service) {
        Validation.notNull(service, "Service cannot be null");
        Validation.notNull(service.getClass(), "Service class cannot be null");
        services.put(service.getClass(), service);
        service.setEnabled();
    }

    public void shutdownServices() {
        services.values().forEach(service -> {
            try {
                service.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        services.clear();
    }

    /**
     * Retrieves a service from the service container.
     *
     * @param serviceClass the class of the service to retrieve
     * @param <T> the type of the service
     * @return the service instance
     * @throws IllegalArgumentException if the service class is null / not found in the container
     */
    public <T extends Service> T getService(Class<T> serviceClass) {
        Validation.notNull(serviceClass, "Service class cannot be null");
        Validation.isFalse(serviceClass == Service.class, "Cannot retrieve Service.class directly. Use a specific service class.");
        Validation.isFalse(serviceClass == NoActionService.class, "Cannot retrieve NoActionService.class directly. Use a specific service class.");
        Validation.notNull(services.get(serviceClass), "Service class " + serviceClass.getName() + " is not registered.");
        Service service = services.get(serviceClass);
        return serviceClass.cast(service);
    }
}
