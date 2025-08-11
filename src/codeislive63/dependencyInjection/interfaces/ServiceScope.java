package codeislive63.dependencyInjection.interfaces;

/**
 * Интерфейс, представляющий область для сервисов.
 */
public interface ServiceScope extends AutoCloseable {

    /**
     * Возвращает провайдера сервисов, связанного с этой областью.
     *
     * @return Провайдер сервисов для данной области.
     */
    ServiceProvider getServiceProvider();

    /**
     * Закрывает область и освобождает все связанные с ней ресурсы.
     * Переопределяет метод из интерфейса {@link AutoCloseable}.
     */
    @Override
    void close();
}
