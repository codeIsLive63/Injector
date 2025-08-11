package dependencyInjection.implementations;

import codeislive63.collections.generic.List;
import dependencyInjection.ServiceDescriptor;
import dependencyInjection.ServiceLifetime;
import dependencyInjection.annotations.Inject;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    private boolean isClosed = false;

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
        if (!isClosed) {
            isClosed = true;
            scopedInstances.clear();
        }
    }

    private void checkIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("ServiceScope уже закрыт.");
        }
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
        checkIfClosed();
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
            Object instance = null;

            if (descriptor.getImplementationInstance() != null) {
                instance = descriptor.getImplementationInstance();
            } else if (descriptor.getImplementationFactory() != null) {
                instance = descriptor.getImplementationFactory().apply(rootProvider);
            } else if (descriptor.getImplementationType() != null) {
                for (Constructor<?> constructor : descriptor.getImplementationType().getConstructors()) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    boolean canInstantiate = true;

                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameters[i] = rootProvider.getService(parameterTypes[i]);
                        if (parameters[i] == null) {
                            canInstantiate = false;
                            break;
                        }
                    }

                    if (canInstantiate) {
                        instance = constructor.newInstance(parameters);
                        break;
                    }

                    throw new IllegalStateException("Не удалось найти подходящий конструктор для " + descriptor.getImplementationType());
                }
            } else {
                throw new IllegalStateException("Неверный дескриптор: " + descriptor);
            }

            if (instance != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.isAnnotationPresent(Inject.class)) {
                        invokeMethodWithDependencies(instance, method);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр сервиса: " + descriptor.getServiceType().getName(), e);
        }
    }

    /**
     * Вызывает метод с внедрением зависимостей.
     *
     * @param target Объект, на котором вызывается метод.
     * @param method Метод, который вызывается.
     */
    private void invokeMethodWithDependencies(Object target, Method method) {
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = rootProvider.getService(parameterTypes[i]);
                if (parameters[i] == null) {
                    throw new IllegalStateException("Не удалось разрешить зависимость для параметра " + parameterTypes[i].getName());
                }
            }

            method.invoke(target, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось вызвать метод с зависимостями: " + method.getName(), e);
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
         * @param serviceDescriptors  Список дескрипторов сервисов.
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

        @Override
        public void close() {
            scope.close();
        }
    }
}
