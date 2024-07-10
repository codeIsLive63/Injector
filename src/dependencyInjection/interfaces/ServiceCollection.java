package dependencyInjection.interfaces;

import collections.generic.ModifiableList;
import delegates.generic.Func;
import dependencyInjection.ServiceDescriptor;

/**
 * Интерфейс, представляющий коллекцию сервисов для конфигурации зависимостей.
 */
public interface ServiceCollection extends ModifiableList<ServiceDescriptor> {

    /**
     * Регистрирует transient зависимость.
     *
     * @param <TAbstract>        Тип абстракции.
     * @param <TImplementation>  Тип реализации, который наследует или реализует {@code TAbstract}.
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    <TAbstract, TImplementation extends TAbstract> ServiceCollection addTransient(Class<TAbstract> abstractType, Class<TImplementation> implementationType);

    /**
     * Регистрирует transient зависимость с помощью фабричного метода.
     *
     * @param <TService>            Тип сервиса.
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addTransient(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory);

    /**
     * Регистрирует transient зависимость с конкретным экземпляром.
     *
     * @param <TService>          Тип сервиса.
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addTransient(Class<TService> implementationClass);

    /**
     * Регистрирует scoped зависимость.
     *
     * @param <TAbstract>        Тип абстракции.
     * @param <TImplementation>  Тип реализации, который наследует или реализует {@code TAbstract}.
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    <TAbstract, TImplementation extends TAbstract> ServiceCollection addScoped(Class<TAbstract> abstractType, Class<TImplementation> implementationType);

    /**
     * Регистрирует scoped зависимость с помощью фабричного метода.
     *
     * @param <TService>            Тип сервиса.
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addScoped(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory);

    /**
     * Регистрирует scoped зависимость для указанного класса сервиса.
     *
     * @param <TService>          Тип сервиса.
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addScoped(Class<TService> implementationClass);

    /**
     * Регистрирует singleton зависимость.
     *
     * @param <TAbstract>        Тип абстракции.
     * @param <TImplementation>  Тип реализации, который наследует или реализует {@code TAbstract}.
     * @param abstractType       Класс абстракции.
     * @param implementationType Класс реализации.
     * @return Текущая коллекция сервисов.
     */
    <TAbstract, TImplementation extends TAbstract> ServiceCollection addSingleton(Class<TAbstract> abstractType, Class<TImplementation> implementationType);

    /**
     * Регистрирует singleton зависимость с помощью фабричного метода.
     *
     * @param <TService>            Тип сервиса.
     * @param serviceType           Класс типа сервиса.
     * @param implementationFactory Фабричный метод для создания реализации.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addSingleton(Class<TService> serviceType, Func<ServiceProvider, TService> implementationFactory);

    /**
     * Регистрирует singleton зависимость для указанного класса сервиса.
     *
     * @param <TService>          Тип сервиса.
     * @param implementationClass Класс типа сервиса.
     * @return Текущая коллекция сервисов.
     */
    <TService> ServiceCollection addSingleton(Class<TService> implementationClass);

    /**
     * Устанавливает пользовательский фабричный метод для создания экземпляров ServiceScope.
     *
     * @param scopeFactory Фабричный метод для создания экземпляров ServiceScope.
     * @return Текущая коллекция сервисов.
     */
    ServiceCollection setScopeFactory(Func<ServiceProvider, ServiceScope> scopeFactory);

    /**
     * Создаёт и возвращает провайдера сервисов.
     *
     * @return Провайдер сервисов.
     */
    ServiceProvider buildServiceProvider();
}
