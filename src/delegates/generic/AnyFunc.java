package delegates.generic;

/**
 * Функциональный интерфейс, представляющий функцию, не принимающую аргументов и возвращающую результат.
 *
 * @param <TResult> Тип результата, возвращаемого функцией.
 */
@FunctionalInterface
public interface AnyFunc<TResult> {

    /**
     * Применяет функцию и возвращает результат.
     *
     * @return Результат применения функции.
     */
    TResult apply();
}