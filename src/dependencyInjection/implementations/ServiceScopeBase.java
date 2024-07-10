package dependencyInjection.implementations;

import collections.generic.List;
import dependencyInjection.ServiceDescriptor;
import dependencyInjection.ServiceLifetime;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовая реализация интерфейса {@link ServiceScope}.
 * <p>
 * Этот класс управляет временем жизни сервисов, зарегистрированных с временем жизни {@code Scoped}.
 * </p>
 */
public class ServiceScopeBase implements ServiceScope {

    private final ServiceProvider rootProvider;
    private final List<ServiceDescriptor> serviceDescriptors;
    private final Map<Class<?>, Object> scopedInstances = new HashMap<>();

    /**
     * Инициализирует новый экземпляр {@link ServiceScopeBase} с указанным корневым провайдером.
     *
     * @param rootProvider Корневой провайдер сервисов.
     * @param serviceDescriptors Список дескрипторов сервисов.
     */
    public ServiceScopeBase(ServiceProvider rootProvider, List<ServiceDescriptor> serviceDescriptors) {
        this.rootProvider = rootProvider;
        this.serviceDescriptors = serviceDescriptors;
    }

    /**
     * Возвращает провайдера сервисов, связанного с этой областью.
     *
     * @return Провайдер сервисов.
     */
    @Override
    public ServiceProvider getServiceProvider() {
        return new ScopedServiceProvider(this, serviceDescriptors);
    }

    /**
     * Закрывает область, освобождая ресурсы.
     */
    @Override
    public void close() {
        scopedInstances.clear();
    }

    /**
     * Возвращает существующий экземпляр сервиса из области или создает новый, если он еще не создан.
     *
     * @param <TService>  Тип сервиса.
     * @param descriptor  Дескриптор сервиса.
     * @return Экземпляр запрашиваемого сервиса.
     */
    @SuppressWarnings("unchecked")
    private <TService> TService getOrCreateService(ServiceDescriptor descriptor) {
        return (TService) scopedInstances.computeIfAbsent(descriptor.getServiceType(), key -> instantiateService(descriptor));
    }

    /**
     * Создает экземпляр сервиса на основе его дескриптора, используя корневой провайдер.
     *
     * @param descriptor  Дескриптор сервиса.
     * @return Экземпляр сервиса.
     */
    private Object instantiateService(ServiceDescriptor descriptor) {
        try {
            if (descriptor.getImplementationInstance() != null) {
                return descriptor.getImplementationInstance();
            } else if (descriptor.getImplementationFactory() != null) {
                return descriptor.getImplementationFactory().apply(rootProvider);
            } else if (descriptor.getImplementationType() != null) {
                return descriptor.getImplementationType().getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalStateException("Неверный дескриптор: " + descriptor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр сервиса: " + descriptor.getServiceType().getName(), e);
        }
    }

    /**
     * Внутренний класс, реализующий {@link ServiceProvider} для области видимости.
     */
    private class ScopedServiceProvider implements ServiceProvider {

        private final ServiceScopeBase scope;
        private final List<ServiceDescriptor> serviceDescriptors;

        /**
         * Инициализирует новый экземпляр {@link ScopedServiceProvider} с указанной областью.
         *
         * @param scope               Область, связанная с этим провайдером сервисов.
         * @param serviceDescriptors Список дескрипторов сервисов.
         */
        public ScopedServiceProvider(ServiceScopeBase scope, List<ServiceDescriptor> serviceDescriptors) {
            this.scope = scope;
            this.serviceDescriptors = serviceDescriptors;
        }

        /**
         * Получает зарегистрированный сервис указанного типа.
         *
         * @param serviceType Класс запрашиваемого сервиса.
         * @return Экземпляр запрашиваемого сервиса или {@code null}, если сервис не зарегистрирован.
         */
        @Override
        public <TService> TService getService(Class<TService> serviceType) {
            for (ServiceDescriptor descriptor : serviceDescriptors) {
                if (descriptor.getServiceType().equals(serviceType) && descriptor.getLifetime() == ServiceLifetime.SCOPED) {
                    return scope.getOrCreateService(descriptor);
                }
            }

            return rootProvider.getService(serviceType);
        }


        /**
         * Получает все зарегистрированные экземпляры указанного типа сервиса.
         *
         * @param serviceType Класс запрашиваемого сервиса.
         * @return Коллекция зарегистрированных экземпляров запрашиваемого сервиса.
         */
        @Override
        public <TService> List<TService> getServices(Class<TService> serviceType) {
            List<TService> result = new List<>();

            for (ServiceDescriptor descriptor : serviceDescriptors) {
                if (descriptor.getServiceType().equals(serviceType) && descriptor.getLifetime() == ServiceLifetime.SCOPED) {
                    result.add(scope.getOrCreateService(descriptor));
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
                throw new IllegalArgumentException("Сервис не зарегистрирован: " + serviceType.getName());
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
            return rootProvider.createScope();
        }
    }
}
