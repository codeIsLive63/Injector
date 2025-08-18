# InjectX

InjectX — лёгкий фреймворк для внедрения зависимостей на Java, обеспечивающий простой и гибкий способ управления зависимостями в приложениях.

[![GitHub Repo](https://img.shields.io/badge/GitHub-Repo-blue?logo=github)](https://github.com/codeislive63/InjectX)

## Возможности и функциональность

InjectX предоставляет лёгкий и гибкий контейнер внедрения зависимостей для Java-приложений. Основные возможности:

*   Поддержка конструкторного и методного внедрения с помощью аннотации `@Inject`.
*   Поддержка сервисов с временем жизни `SINGLETON`, `SCOPED` и `TRANSIENT`.
*   Поддержка создания областей сервисов (scopes) для управления сервисами с областью действия.
*   Регистрация сервисов через фабричные методы.
*   Автоматическое разрешение зависимостей для конструкторов.

## Технологический стек

*   Java 8+
*   JUnit 5 (для тестирования)

## Требования

*   Java Development Kit (JDK) 8 или выше.
*   Maven или Gradle (опционально, для сборки из исходников).

## Инструкция по установке

1.  **Клонируйте репозиторий:**

    ```bash
    git clone https://github.com/codeislive63/InjectX.git
    cd InjectX
    ```

2.  **Соберите проект (опционально):**

    Если вы хотите собрать проект из исходников:

    ```bash
    # Используя Maven
    mvn clean install

    # Или Gradle (если будут добавлены Gradle build файлы)
    gradle clean build
    ```

3.  **Подключить библиотеку к проекту:**

    *   **Если вы собрали проект:** Добавьте сгенерированный JAR-файл (он появится в папке `target` после сборки Maven) в classpath вашего проекта.

    *   **Вручную:** Скопируйте исходные файлы из папки `src` в директорию с исходниками вашего проекта.

## Руководство по использованию

### 1. Регистрация сервисов

Используйте интерфейс `ServiceCollection` и его реализации (например, `ServiceCollectionBase`) для регистрации сервисов.

```java
import codeislive63.dependencyInjection.implementations.ServiceCollectionBase;
import codeislive63.dependencyInjection.interfaces.ServiceCollection;
import codeislive63.dependencyInjection.interfaces.ServiceProvider;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.LoggingService;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.LoggingServiceImplementation;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.DataService;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.DataServiceImplementation;

public class Example {
    public static void main(String[] args) {
        ServiceCollection services = new ServiceCollectionBase();

        // Регистрация transient-сервиса (новый экземпляр каждый раз)
        services.addTransient(LoggingService.class, LoggingServiceImplementation.class);

        // Регистрация scoped-сервиса (один экземпляр на область)
        services.addScoped(DataService.class, DataServiceImplementation.class);

        // Регистрация singleton-сервиса (один экземпляр на всё приложение)
        services.addSingleton(DataService.class, DataServiceImplementation.class);

        // Создание поставщика сервисов
        ServiceProvider serviceProvider = services.buildServiceProvider();

        // ... Дальнейшее использование serviceProvider для разрешения зависимостей ...
    }
}
```

### 2. Получение зависимостей

Используйте интерфейс `ServiceProvider` для получения зарегистрированных сервисов.

```java
import codeislive63.dependencyInjection.interfaces.ServiceProvider;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.LoggingService;
import codeislive63.dependencyInjection.interfaces.ServiceScope;

public class Example {
    public static void main(String[] args) {
        // (Предполагается, что serviceProvider уже создан как выше)
        ServiceProvider serviceProvider = null; // Замените на реальный экземпляр

        // Получение сервиса
        LoggingService loggingService = serviceProvider.getService(LoggingService.class);

        if (loggingService != null) {
            loggingService.log("Hello, world!");
        }

        // Создание области
        try (ServiceScope scope = serviceProvider.createScope()) {
            ServiceProvider scopedProvider = scope.getServiceProvider();
            DataService dataService = scopedProvider.getService(DataService.class);

            if(dataService != null){
                System.out.println(dataService.getData());
            }
        }

        serviceProvider.close(); // Важно: закройте serviceProvider для освобождения ресурсов.
    }
}
```

### 3. Внедрение через конструктор

Возможно использование аннотации `@Inject` на конструкторах для возможности использования внедрения через конструктор. Однако данная аннотация необязательна — зависимости будут внедрены автоматически. Её можно использовать для явного указания, какой именно конструктор должен применяться при создании экземпляра.

```java
import codeislive63.dependencyInjection.annotations.Inject;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.LoggingService;

public class MyService {
    private final LoggingService loggingService;

    @Inject // Необязательно для конструкторов
    public MyService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void doSomething() {
        loggingService.log("Doing something...");
    }
}
```

### 4. Внедрение через метод

Используйте аннотацию `@Inject` на методах для включения методного внедрения.

```java
import codeislive63.dependencyInjection.annotations.Inject;
import codeislive63.test.java.dependencyInjection.serviceCollectionTests.services.DataService;

public class MyComponent {
    private DataService dataService;

    @Inject
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public void processData() {
        System.out.println("Data: " + dataService.getData());
    }
}
```

### 5. Время жизни сервисов

*   **`TRANSIENT`:** Новый экземпляр создаётся при каждом обращении.  
*   **`SCOPED`:** Новый экземпляр создаётся один раз для области. Вы можете создать область, используя: `ServiceProvider.createScope()`.  
*   **`SINGLETON`:** Один экземпляр создаётся на всё время жизни `ServiceProvider`. В большинстве случаев это соответствует времени работы всего приложения.

## API Документация

### Интерфейсы

*   **`ServiceCollection`:** Определяет интерфейс для регистрации сервисов.  
    *   `addTransient(Class<TAbstract> abstractType, Class<TImplementation> implementationType)`: Регистрирует transient-сервис.  
    *   `addScoped(Class<TAbstract> abstractType, Class<TImplementation> implementationType)`: Регистрирует scoped-сервис.  
    *   `addSingleton(Class<TAbstract> abstractType, Class<TImplementation> implementationType)`: Регистрирует singleton-сервис.  
    *   `buildServiceProvider()`: Создаёт экземпляр `ServiceProvider`.  

*   **`ServiceProvider`:** Определяет интерфейс для получения сервисов.  
    *   `getService(Class<TService> serviceType)`: Возвращает сервис указанного типа или `null`, если он не зарегистрирован.  
    *   `getRequiredService(Class<TService> serviceType)`: Возвращает сервис указанного типа или выбрасывает исключение, если он не зарегистрирован.  
    *   `createScope()`: Создаёт новый `ServiceScope`.  
    *   `close()`: Закрывает `ServiceProvider` и освобождает ресурсы.  

*   **`ServiceScope`:** Определяет интерфейс для области сервисов.  
    *   `getServiceProvider()`: Возвращает `ServiceProvider`, связанный с областью.  
    *   `close()`: Закрывает область и освобождает ресурсы.  

### Классы

*   **`ServiceCollectionBase`:** Базовая реализация `ServiceCollection`.  
*   **`ServiceProviderBase`:** Базовая реализация `ServiceProvider`.  
*   **`ServiceScopeBase`:** Базовая реализация `ServiceScope`.  
*   **`ServiceDescriptor`:** Представляет описание сервиса (тип, реализация, время жизни, фабрика).  
*   **`ServiceLifetime`:** Перечисление с временами жизни (`SINGLETON`, `SCOPED`, `TRANSIENT`).  

### Аннотации

*   **`@Inject`:** Используется для пометки конструкторов или методов для внедрения зависимостей.  

## Контактная информация

Если у вас есть вопросы, вы можете сообщить об ошибках или предложить новые функции. Для этого создайте задачу в [репозитории GitHub](https://github.com/codeislive63/InjectX).
