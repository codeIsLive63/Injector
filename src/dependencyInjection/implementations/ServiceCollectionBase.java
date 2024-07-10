package dependencyInjection.implementations;

import collections.generic.Enumerator;
import collections.generic.List;
import delegates.generic.Func;
import dependencyInjection.ServiceDescriptor;
import dependencyInjection.ServiceLifetime;
import dependencyInjection.interfaces.ServiceCollection;
import dependencyInjection.interfaces.ServiceProvider;
import dependencyInjection.interfaces.ServiceScope;
import jenumerable.JEnumerable;

/**
 * Базовая реализация интерфейса {@link ServiceCollection}.
 * <p>
 * Этот класс предоставляет методы для регистрации зависимостей различных типов
 * (transient, scoped и singleton) и создания провайдера сервисов.
 * </p>
 */
public class ServiceCollectionBase implements ServiceCollection {

    private final List<ServiceDescriptor> services = new List<>();
    private Func<ServiceProvider, ServiceScope> scopeFactory;

    /**
     * Регистрирует transient зависимость.
     *
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TAbstract, TImplementation extends TAbstract> ServiceCollection addTransient(Class<TAbstract> abstractType, Class<TImplementation> implementationType) {
        services.add(new ServiceDescriptor(abstractType, implementationType, ServiceLifetime.TRANSIENT));
        return this;
    }

    /**
     * Регистрирует transient зависимость с помощью фабричного метода.
     *
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addTransient(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory) {
        services.add(new ServiceDescriptor(serviceType, implementationFactory, ServiceLifetime.TRANSIENT));
        return this;
    }

    /**
     * Регистрирует transient зависимость с конкретным экземпляром.
     *
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addTransient(Class<TService> implementationClass) {
        services.add(new ServiceDescriptor(implementationClass, implementationClass, ServiceLifetime.TRANSIENT));
        return this;
    }

    /**
     * Регистрирует scoped зависимость.
     *
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TAbstract, TImplementation extends TAbstract> ServiceCollection addScoped(Class<TAbstract> abstractType, Class<TImplementation> implementationType) {
        services.add(new ServiceDescriptor(abstractType, implementationType, ServiceLifetime.SCOPED));
        return this;
    }

    /**
     * Регистрирует scoped зависимость с помощью фабричного метода.
     *
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addScoped(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory) {
        services.add(new ServiceDescriptor(serviceType, implementationFactory, ServiceLifetime.SCOPED));
        return this;
    }

    /**
     * Регистрирует scoped зависимость для указанного класса сервиса.
     *
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addScoped(Class<TService> implementationClass) {
        services.add(new ServiceDescriptor(implementationClass, implementationClass, ServiceLifetime.SCOPED));
        return this;
    }

    /**
     * Регистрирует singleton зависимость.
     *
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TAbstract, TImplementation extends TAbstract> ServiceCollection addSingleton(Class<TAbstract> abstractType, Class<TImplementation> implementationType) {
        services.add(new ServiceDescriptor(abstractType, implementationType, ServiceLifetime.SINGLETON));
        return this;
    }

    /**
     * Регистрирует singleton зависимость с помощью фабричного метода.
     *
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addSingleton(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory) {
        services.add(new ServiceDescriptor(serviceType, implementationFactory, ServiceLifetime.SINGLETON));
        return this;
    }

    /**
     * Регистрирует singleton зависимость для указанного класса сервиса.
     *
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public <TService> ServiceCollection addSingleton(Class<TService> implementationClass) {
        services.add(new ServiceDescriptor(implementationClass, implementationClass, ServiceLifetime.SINGLETON));
        return this;
    }

    /**
     * Устанавливает пользовательский фабричный метод для создания экземпляров ServiceScope.
     *
     * @param scopeFactory Фабричный метод для создания экземпляров ServiceScope.
     * @return Текущая коллекция сервисов.
     */
    @Override
    public ServiceCollection setScopeFactory(Func<ServiceProvider, ServiceScope> scopeFactory) {
        this.scopeFactory = scopeFactory;
        return this;
    }

    /**
     * Создаёт и возвращает провайдера сервисов.
     *
     * @return Провайдер сервисов.
     */
    @Override
    public ServiceProvider buildServiceProvider() {
        boolean isRequiredScopeFactory = JEnumerable.from(services)
                .any(serviceDescriptor -> serviceDescriptor.getLifetime() == ServiceLifetime.SCOPED);

        if (isRequiredScopeFactory && scopeFactory == null) {
            scopeFactory = rootProvider -> new ServiceScopeBase(rootProvider, services);
        }

        return new ServiceProviderBase(services, scopeFactory);
    }

    /**
     * Получает элемент по указанному индексу в списке.
     *
     * @param index Индекс извлекаемого элемента.
     * @return Элемент по указанному индексу.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы допустимого диапазона.
     */
    @Override
    public ServiceDescriptor get(int index) {
        return services.get(index);
    }

    /**
     * Устанавливает элемент с указанным индексом в списке на заданный элемент.
     *
     * @param index Индекс, по которому должен быть установлен элемент.
     * @param item  Элемент, который должен быть установлен по указанному индексу.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы допустимого диапазона.
     */
    @Override
    public void set(int index, ServiceDescriptor item) {
        services.set(index, item);
    }

    /**
     * Возвращает индекс первого появления указанного элемента в списке.
     *
     * @param item Элемент для поиска.
     * @return Индекс первого вхождения указанного элемента или -1, если элемент не найден в списке.
     */
    @Override
    public int indexOf(ServiceDescriptor item) {
        return services.indexOf(item);
    }

    /**
     * Удаляет элемент по указанному индексу из списка.
     *
     * @param index Индекс удаляемого элемента.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы допустимого диапазона.
     */
    @Override
    public void removeAt(int index) {
        services.removeAt(index);
    }

    /**
     * Получает количество элементов в коллекции.
     *
     * @return Количество элементов в коллекции.
     */
    @Override
    public int count() {
        return services.count();
    }

    /**
     * Добавляет элемент в коллекцию.
     *
     * @param item Элемент, который нужно добавить в коллекцию.
     */
    @Override
    public void add(ServiceDescriptor item) {
        services.add(item);
    }

    /**
     * Удаляет все элементы из коллекции.
     * Коллекция будет пустой после вызова этого метода.
     */
    @Override
    public void clear() {
        services.clear();
    }

    /**
     * Проверяет, содержит ли коллекция указанный элемент.
     *
     * @param item Элемент, который нужно проверить на наличие в коллекции.
     * @return {@code true}, если коллекция содержит элемент, {@code false} в противном случае.
     */
    @Override
    public boolean contains(ServiceDescriptor item) {
        return services.contains(item);
    }

    /**
     * Удаляет единичное вхождение указанного элемента из коллекции, если он присутствует.
     *
     * @param item Элемент, который необходимо удалить из коллекции.
     */
    @Override
    public void remove(ServiceDescriptor item) {
        services.remove(item);
    }

    /**
     * Возвращает перечислитель элементов коллекции.
     *
     * @return {@link Enumerator}, который можно использовать для перебора элементов.
     */
    @Override
    public Enumerator<ServiceDescriptor> getEnumerator() {
        return services.getEnumerator();
    }
}
