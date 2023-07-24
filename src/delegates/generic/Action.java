package delegates.generic;

/**
 * Представляет действие, которое принимает один аргумент и не возвращает результата.
 *
 * @param <T> Тип аргумента действия.
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Выполняет действие с заданным аргументом.
     *
     * @param arg Аргумент действия.
     */
    void invoke(T arg);
}