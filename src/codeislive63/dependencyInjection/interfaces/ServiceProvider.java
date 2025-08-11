package codeislive63.dependencyInjection.interfaces;

import codeislive63.collections.generic.Enumerable;

/**
 * Интерфейс, представляющий провайдера сервисов для конфигурации зависимостей.
 */
public interface ServiceProvider extends AutoCloseable {

    /**
     * Получает зарегистрированный сервис указанного типа.
     *
     * @param <TService>  Тип запрашиваемого сервиса.
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Экземпляр запрашиваемого сервиса или {@code null}, если сервис не зарегистрирован.
     */
    <TService> TService getService(Class<TService> serviceType);

    /**
     * Получает все зарегистрированные экземпляры указанного типа сервиса.
     *
     * @param <TService>  Тип запрашиваемого сервиса.
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Коллекция зарегистрированных экземпляров запрашиваемого сервиса.
     */
    <TService> Enumerable<TService> getServices(Class<TService> serviceType);

    /**
     * Получает зарегистрированный сервис указанного типа. Если сервис не зарегистрирован,
     * выбрасывает исключение.
     *
     * @param <TService>  Тип запрашиваемого сервиса.
     * @param serviceType Класс запрашиваемого сервиса.
     * @return Экземпляр запрашиваемого сервиса.
     * @throws IllegalArgumentException Если сервис не зарегистрирован.
     */
    <TService> TService getRequiredService(Class<TService> serviceType);

    /**
     * Создает новую область для сервисов.
     *
     * @return Новый экземпляр области для сервисов.
     */
    ServiceScope createScope();

    /**
     * Закрывает область и освобождает все связанные с ней ресурсы.
     * Переопределяет метод из интерфейса {@link AutoCloseable}.
     */
    @Override
    void close();
}
