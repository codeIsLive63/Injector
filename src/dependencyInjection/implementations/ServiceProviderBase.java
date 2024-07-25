package dependencyInjection.implementations;

import collections.generic.Enumerable;
import collections.generic.List;
import delegates.generic.Func;
import dependencyInjection.ServiceDescriptor;
import dependencyInjection.annotations.Inject;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    private boolean isClosed = false;

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
        checkIfClosed();

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
        checkIfClosed();

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
        checkIfClosed();

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
        checkIfClosed();
        return scopeFactory.apply(this);
    }

    @SuppressWarnings("unchecked")
    private <TService> TService createService(ServiceDescriptor descriptor) {
        return switch (descriptor.getLifetime()) {
            case SINGLETON -> (TService) getOrCreateSingletonInstance(descriptor);
            case SCOPED, TRANSIENT -> (TService) instantiateService(descriptor);
        };
    }

    /**
     * Создаёт или возвращает существующий экземпляр singleton сервиса.
     *
     * @param descriptor Дескриптор сервиса.
     * @return Экземпляр сервиса.
     */
    private synchronized Object getOrCreateSingletonInstance(ServiceDescriptor descriptor) {
        checkIfClosed();

        if (!singletonInstances.containsKey(descriptor.getServiceType())) {
            Object instance = instantiateService(descriptor);
            singletonInstances.put(descriptor.getServiceType(), instance);
        }

        return singletonInstances.get(descriptor.getServiceType());
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
            Object instance = null;

            if (descriptor.getImplementationInstance() != null) {
                instance = descriptor.getImplementationInstance();
            } else if (descriptor.getImplementationFactory() != null) {
                instance = descriptor.getImplementationFactory().apply(this);
            } else if (descriptor.getImplementationType() != null) {
                for (Constructor<?> constructor : descriptor.getImplementationType().getConstructors()) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    boolean canInstantiate = true;

                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameters[i] = getService(parameterTypes[i]);
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
                parameters[i] = getService(parameterTypes[i]);
                if (parameters[i] == null) {
                    throw new IllegalStateException("Не удалось разрешить зависимость для параметра " + parameterTypes[i].getName());
                }
            }

            method.invoke(target, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось вызвать метод с зависимостями: " + method.getName(), e);
        }
    }

    @Override
    public void close() {
        if (!isClosed) {
            isClosed = true;
            singletonInstances.clear();
        }
    }

    private void checkIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("ServiceProvider уже закрыт.");
        }
    }
}
