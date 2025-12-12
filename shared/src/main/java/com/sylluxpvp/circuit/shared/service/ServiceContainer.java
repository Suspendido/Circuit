package com.sylluxpvp.circuit.shared.service;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.tools.java.ClassUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceContainer is a utility class that manages the registration and retrieval of services inside Circuit.
 * It allows services to be registered and later retrieved by their class type.
 * <p>
 * NOTE: Class was reused from an older project of mine, Nugget, and modified to fit Circuit's needs.
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
        Validate.notNull(clazz, "Class cannot be null");
        Validate.isTrue(Service.class.isAssignableFrom(clazz), "Class " + clazz.getName() + " is not a Repository.");

        try {
            return (Service) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create service instance for " + clazz.getName(), e);
        }
    }

    @SneakyThrows
    private void registerServices() {
        List<Class<?>> classes = ClassUtils.getClasses(CircuitShared.getInstance().getFile(), Service.class.getPackage().getName() + ".impl");

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
        Validate.notNull(service, "Service cannot be null");
        Validate.notNull(service.getClass(), "Service class cannot be null");
        for (Class<? extends Service> dependencyIdentifier : service.getDependencies()) {
            if (services.containsKey(dependencyIdentifier)) continue;

            Service depInstance = newServiceInstance(dependencyIdentifier);
            registerService(depInstance);
        }
        services.put(service.getClass(), service);
        service.setEnabled();
    }

    public void shutdownServices() {
        services.values().forEach(ServiceContainer::shutdownService);
        services.clear();
    }

    @SneakyThrows
    public void shutdownService(Service service) {
        Validate.notNull(service, "Service cannot be null");
        service.setEnabled(false);
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
        Validate.notNull(serviceClass, "Service class cannot be null");
        Validate.isTrue(serviceClass != Service.class, "Cannot retrieve Service.class directly. Use a specific service class.");
        Validate.isTrue(serviceClass != NoActionService.class, "Cannot retrieve NoActionService.class directly. Use a specific service class.");
        Validate.notNull(services.get(serviceClass), "Service class " + serviceClass.getName() + " is not registered.");
        Service service = services.get(serviceClass);
        return serviceClass.cast(service);
    }

}
