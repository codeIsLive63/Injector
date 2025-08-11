package codeislive63.dependencyInjection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;

/**
 * Обозначает, что аннотированный конструктор или метод должны быть использованы
 * для внедрения зависимостей.
 *
 * <p>Эта аннотация используется для маркировки конструктора или метода как
 * цели для внедрения зависимостей. Когда фреймворк внедрения зависимостей
 * обнаруживает эту аннотацию, он автоматически внедрит необходимые зависимости
 * в аннотированный элемент.</p>
 *
 * <p>Для конструкторов использование этой аннотации не является обязательным,
 * так как фреймворк автоматически предоставляет зависимости для них.
 * Однако, её использование может быть полезным для явного указания,
 * какой именно конструктор должен использоваться для внедрения зависимостей.</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * public class MyService {
 *
 *     private final LoggingService loggingService;
 *     private DataService dataService;
 *
 *     // Внедрение зависимости через конструктор, аннотация не обязательна
 *     public MyService(LoggingService loggingService) {
 *         this.loggingService = loggingService;
 *     }
 *
 *     @Inject
 *     public void setDataService(DataService dataService) {
 *         this.dataService = dataService;
 *     }
 *
 *     public void performAction() {
 *         loggingService.log("Начинаю выполнение действия по получению данных...");
 *         Data data = dataService.getData();
 *         loggingService.log("Данные: " + data);
 *     }
 * }
 * }
 * </pre>
 *
 * @see java.lang.annotation.ElementType
 * @see java.lang.annotation.RetentionPolicy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Inject {

}
