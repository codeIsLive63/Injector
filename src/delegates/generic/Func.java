package delegates.generic;

/**
 * Функциональный интерфейс, представляющий функцию, которая принимает один аргумент типа T1,
 * и выдает результат типа TResult.
 *
 * @param <T1>      Тип входного аргумента.
 * @param <TResult> Тип результата, полученного функцией.
 */
@FunctionalInterface
public interface Func<T1, TResult> {

    /**
     * Применяет функцию к заданному входному аргументу.
     *
     * @param arg Входной аргумент функции.
     * @return Результат, полученный путем применения функции к входному аргументу.
     */
    TResult apply(T1 arg);
}