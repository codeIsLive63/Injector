package collections.generic;

import java.util.Iterator;

/**
 * Интерфейс {@code IEnumerable} — это универсальный интерфейс, представляющий набор элементов,
 * которые можно перечислить. Он расширяет интерфейс {@link Iterable}, предоставляя итератор для элементов.
 * Он также предоставляет метод для получения перечислителя, который позволяет проходить коллекцию.
 *
 * @param <T> Тип элементов в коллекции.
 */
public interface Enumerable<T> extends Iterable<T> {

    /**
     * Возвращает перечислитель элементов коллекции.
     *
     * @return {@link Enumerator}, который можно использовать для перебора элементов.
     */
    Enumerator<T> getEnumerator();

    /**
     * Возвращает итератор по элементам коллекции. Этот метод предназначен для поддержки
     * расширенного синтаксиса цикла for ({@code for-each}) в Java.
     *
     * @return {@link Iterator}, который можно использовать для перебора элементов.
     */
    default Iterator<T> iterator() {
        return getEnumerator().asIterator();
    }
}