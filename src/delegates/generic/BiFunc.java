package delegates.generic;

/**
 * Представляет функциональный интерфейс, который принимает два аргумента типов T1 и T2,
 * и выдает результат типа TResult.
 *
 * @param <T1>      Тип первого аргумента.
 * @param <T2>      Тип второго аргумента.
 * @param <TResult> Тип результата.
 */
@FunctionalInterface
public interface BiFunc<T1, T2, TResult> {

    /**
     * Применяет функцию к заданным аргументам.
     *
     * @param arg1 Первый аргумент.
     * @param arg2 Второй аргумент.
     * @return Результат вычисления функции.
     */
    TResult apply(T1 arg1, T2 arg2);
}
