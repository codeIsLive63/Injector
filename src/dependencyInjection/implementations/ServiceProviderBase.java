package dependencyInjection.implementations;

import collections.generic.Enumerable;
import collections.generic.List;
import delegates.generic.Func;
import dependencyInjection.ServiceDescriptor;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовая реализация интерфейса {@link ServiceProvider}.
 * <p>
 * Этот класс предоставляет методы для получения зарегистрированных сервисов и создания областей.
 * </p>
 */
public class ServiceProviderBase implements ServiceProvider {

    private final List<ServiceDescriptor> services;
    private final Func<ServiceProvider, ServiceScope> scopeFactory;
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();

    /**
     * Инициализирует новый экземпляр {@link ServiceProviderBase} с указанными
     * зарегистрированными сервисами и фабричным методом для создания областей.
     *
     * @param services     Коллекция зарегистрированных сервисов.
     * @param scopeFactory Пользовательский фабричный метод для создания экземпляров ServiceScope.
     */
    public ServiceProviderBase(List<ServiceDescriptor> services, Func<ServiceProvider, ServiceScope> scopeFactory) {
        this.services = services;
        this.scopeFactory = scopeFactory;
    }

    /**
     * Возвращает все зарегистрированные дескрипторы сервисов.
     *
     * @return Коллекция всех дескрипторов сервисов.
     */
    public List<ServiceDescriptor> getServiceDescriptors() {
        return services;
    }

    /**
     * Получает зарегистрированный сервис указанного типа.
     *
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Экземпляр запрашиваемого сервиса или {@code null}, если сервис не зарегистрирован.
     */
    @Override
    public <TService> TService getService(Class<TService> serviceType) {
        for (ServiceDescriptor descriptor : services) {
            if (descriptor.getServiceType().equals(serviceType)) {
                return createService(descriptor);
            }
        }

        return null;
    }

    /**
     * Получает все зарегистрированные экземпляры указанного типа сервиса.
     *
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Коллекция зарегистрированных экземпляров запрашиваемого сервиса.
     */
    @Override
    public <TService> Enumerable<TService> getServices(Class<TService> serviceType) {
        List<TService> result = new List<>();

        for (ServiceDescriptor descriptor : services) {
            if (descriptor.getServiceType().equals(serviceType)) {
                result.add(createService(descriptor));
            }
        }

        return result;
    }

    /**
     * Получает зарегистрированный сервис указанного типа. Если сервис не зарегистрирован,
     * выбрасывает исключение.
     *
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Экземпляр запрашиваемого сервиса.
     * @throws IllegalArgumentException Если сервис не зарегистрирован.
     */
    @Override
    public <TService> TService getRequiredService(Class<TService> serviceType) {
        TService service = getService(serviceType);

        if (service == null) {
            throw new IllegalArgumentException("Сервис не зарегистрирован " + serviceType.getName());
        }

        return service;
    }

    /**
     * Создает новую область для сервисов.
     *
     * @return Новый экземпляр области для сервисов.
     */
    @Override
    public ServiceScope createScope() {
        return scopeFactory.apply(this);
    }

    /**
     * Создает экземпляр сервиса на основе его дескриптора.
     *
     * @param descriptor Дескриптор сервиса.
     * @return Экземпляр запрашиваемого сервиса.
     */
    @SuppressWarnings("unchecked")
    private <TService> TService createService(ServiceDescriptor descriptor) {
        return switch (descriptor.getLifetime()) {
            case SINGLETON -> (TService) singletonInstances.computeIfAbsent(
                    descriptor.getServiceType(), key -> instantiateService(descriptor)
            );

            case SCOPED, TRANSIENT -> (TService) instantiateService(descriptor);
        };
    }

    /**
     * Создаёт экземпляр сервиса на основе его дескриптора.
     *
     * @param descriptor Дескриптор сервиса.
     * @return Экземпляр сервиса.
     * @throws RuntimeException Если не удалось создать экземпляр сервиса.
     */
    private Object instantiateService(ServiceDescriptor descriptor) {
        try {
            if (descriptor.getImplementationInstance() != null) {
                return descriptor.getImplementationInstance();
            } else if (descriptor.getImplementationFactory() != null) {
                return descriptor.getImplementationFactory().apply(this);
            } else if (descriptor.getImplementationType() != null) {
                return descriptor.getImplementationType().getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalStateException("Неверный дескриптор: " + descriptor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр сервиса: " + descriptor.getServiceType().getName(), e);
        }
    }
}
