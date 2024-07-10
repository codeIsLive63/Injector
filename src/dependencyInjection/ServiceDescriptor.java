package dependencyInjection;

import delegates.generic.Func;
import dependencyInjection.interfaces.ServiceProvider;

/**
 * Класс, представляющий дескриптор сервиса, используемый для регистрации сервисов
 * в контейнере зависимостей. Содержит информацию о типе сервиса, типе реализации,
 * экземпляре реализации, фабричных методах и времени жизни сервиса.
 */
public class ServiceDescriptor {

    private final Class<?> serviceType;
    private final Class<?> implementationType;
    private final Object implementationInstance;
    private final Func<ServiceProvider, ?> implementationFactory;
    private final ServiceLifetime lifetime;

    /**
     * Инициализирует новый экземпляр {@link ServiceDescriptor} с указанным фабричным методом.
     *
     * @param serviceType           Тип сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @param lifetime              Время жизни сервиса.
     */
    public ServiceDescriptor(Class<?> serviceType, Func<ServiceProvider, ?> implementationFactory, ServiceLifetime lifetime) {
        this.serviceType = serviceType;
        this.implementationType = null;
        this.implementationInstance = null;
        this.implementationFactory = implementationFactory;
        this.lifetime = lifetime;
    }

    /**
     * Инициализирует новый экземпляр {@link ServiceDescriptor} с указанным типом реализации.
     *
     * @param serviceType        Тип сервиса.
     * @param implementationType Тип реализации сервиса.
     * @param lifetime           Время жизни сервиса.
     */
    public ServiceDescriptor(Class<?> serviceType, Class<?> implementationType, ServiceLifetime lifetime) {
        this.serviceType = serviceType;
        this.implementationType = implementationType;
        this.implementationInstance = null;
        this.implementationFactory = null;
        this.lifetime = lifetime;
    }

    /**
     * Возвращает тип сервиса.
     *
     * @return Тип сервиса.
     */
    public Class<?> getServiceType() {
        return serviceType;
    }

    /**
     * Возвращает тип реализации сервиса.
     *
     * @return Тип реализации сервиса.
     */
    public Class<?> getImplementationType() {
        return implementationType;
    }

    /**
     * Возвращает экземпляр реализации сервиса.
     *
     * @return Экземпляр реализации сервиса.
     */
    public Object getImplementationInstance() {
        return implementationInstance;
    }

    /**
     * Возвращает фабричный метод для создания реализации сервиса.
     *
     * @return Фабричный метод для создания реализации сервиса.
     */
    public Func<ServiceProvider, ?> getImplementationFactory() {
        return implementationFactory;
    }

    /**
     * Возвращает время жизни сервиса.
     *
     * @return Время жизни сервиса.
     */
    public ServiceLifetime getLifetime() {
        return lifetime;
    }
}
