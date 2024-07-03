package collections.generic;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Представляет строго типизированный список объектов.
 * Этот класс реализует интерфейсы IList, ICollection и IEnumerable.
 *
 * @param <T> Тип элементов, хранящихся в списке.
 */
public class List<T> implements ModifiableList<T>, Collection<T>, Enumerable<T> {

    private T[] _items;

    private int _count;

    /**
     * Создает пустой список.
     */
    @SuppressWarnings("unchecked")
    public List() {
        _items = (T[]) new Object[]{ };
        _count = 0;
    }

    /**
     * Создает список из массива элементов.
     *
     * @param collection Массив элементов для инициализации списка.
     * @throws IllegalArgumentException Если входной массив пуст.
     */
    @SafeVarargs
    public List(T... collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Входной массив пуст.");
        }

        _items = collection;
        _count = collection.length;
    }

    /**
     * Создает список на основе элементов из Iterable.
     *
     * @param collection Коллекция элементов Iterable для инициализации списка.
     * @throws IllegalArgumentException Если входная коллекция пуста.
     */
    @SuppressWarnings("unchecked")
    public List(Iterable<T> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Входная коллекция пуста.");
        }

        _count = 0;

        for (T ignored : collection) {
            _count++;
        }

        if (_count == 0) {
            _items = (T[]) new Object[0];
            return;
        }

        _items = (T[]) new Object[_count];

        int index = 0;

        for (T item : collection) {
            _items[index] = item;
            index++;
        }
    }

    /**
     * Создает список из коллекции элементов IEnumerable.
     *
     * @param collection Коллекция элементов IEnumerable для инициализации списка.
     * @throws IllegalArgumentException Если входная коллекция пуста.
     */
    @SuppressWarnings("unchecked")
    public List(Enumerable<T> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Входная коллекция пуста.");
        }

        _count = 0;

        for (T ignored : collection) {
            _count++;
        }

        if (_count == 0) {
            _items = (T[]) new Object[0];
            return;
        }

        _items = (T[]) new Object[_count];

        int index = 0;

        for (T item : collection) {
            _items[index] = item;
            index++;
        }
    }

    /**
     * Возвращает перечислитель для перебора элементов списка.
     *
     * @return Перечислитель типа T.
     */
    @Override
    public Enumerator<T> getEnumerator() {
        return new ListEnumerator();
    }

    /**
     * Возвращает количество элементов в списке.
     *
     * @return Количество элементов в списке.
     */
    @Override
    public int count() {
        return _count;
    }

    /**
     * Добавляет элемент в конец списка.
     *
     * @param item Элемент для добавления в список.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(T item) {
        T[] newItems = (T[]) new Object[_count + 1];

        System.arraycopy(_items, 0, newItems, 0, _count);
        newItems[_count] = item;

        _items = newItems;
        _count++;
    }

    /**
     * Удаляет все элементы из списка.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        _count = 0;
        _items = (T[]) new Object[0];
    }

    /**
     * Проверяет, содержит ли список указанный элемент.
     *
     * @param item T Элемент для проверки в списке.
     * @return {@code true} если элемент найден, иначе {@code false}.
     */
    @Override
    public boolean contains(T item) {
        for (int i = 0; i < _count; i++) {
            if(_items[i].equals(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Удаляет первое вхождение указанного элемента из списка.
     *
     * @param item Элемент, который необходимо удалить из списка.
     */
    @Override
    public void remove(T item) {
        int index = indexOf(item);

        if (index >= 0) {
            removeAt(index);
        }
    }

    /**
     * Получает элемент по указанному индексу в списке.
     *
     * @param index Индекс извлекаемого элемента.
     * @return Элемент по указанному индексу.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы диапазона.
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= _count) {
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");
        }

        return _items[index];
    }

    /**
     * Устанавливает элемент с указанным индексом в списке на указанное значение.
     *
     * @param index Индекс устанавливаемого элемента.
     * @param item Новое значение для установки по указанному индексу.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы диапазона.
     */
    @Override
    public void set(int index, T item) {
        if(index < 0 || index >= _count) {
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");
        }

        _items[index] = item;
    }

    /**
     * Ищет указанный элемент в списке и возвращает его индекс.
     *
     * @param item Элемент для поиска в списке.
     * @return Индекс первого вхождения элемента или -1, если он не найден.
     */
    @Override
    public int indexOf(T item) {
        for (int i = 0; i < _count; i++) {
            if(_items[i].equals(item)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Удаляет элемент по указанному индексу из списка.
     *
     * @param index Индекс удаляемого элемента.
     * @throws IndexOutOfBoundsException Если индекс выходит за пределы диапазона.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void removeAt(int index) {
        if (index < 0 || index >= _count) {
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");
        }

        T[] newItems = (T[]) new Object[_count - 1];

        System.arraycopy(_items, 0, newItems, 0, index);
        System.arraycopy(_items, index + 1, newItems, index, _count - index - 1);

        _items = newItems;
        _count--;
    }

    /**
     * Преобразует коллекцию List в последовательный поток элементов.
     *
     * @return Последовательный поток элементов коллекции.
     */
    public Stream<T> asStream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(getEnumerator().asIterator(), Spliterator.ORDERED),
                false
        );
    }

    /**
     * Преобразует коллекцию List в параллельный поток элементов.
     *
     * @return Параллельный поток элементов коллекции.
     */
    public Stream<T> asParallelStream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(getEnumerator().asIterator(), Spliterator.ORDERED),
                true
        );
    }

    /**
     * Внутренний класс, который реализует интерфейс IEnumerator для перебора элементов списка.
     */
    private class ListEnumerator implements Enumerator<T> {

        private int _currentIndex;

        public ListEnumerator() {
            _currentIndex = -1;
        }

        /**
         * Перемещает перечислитель к следующему элементу в списке.
         *
         * @return {@code true} если перечислитель успешно продвинулся к следующему элементу; {@code false} если перечислитель прошел конец списка.
         */
        @Override
        public boolean moveNext() {
            _currentIndex++;
            return _currentIndex < _count;
        }

        /**
         * Возвращает текущий элемент в списке.
         *
         * @return Текущий элемент в списке.
         * @throws NoSuchElementException Если перечислитель расположен перед первым элементом списка или после последнего элемента.
         */
        @Override
        public T getCurrent() {
            if (_currentIndex < 0 || _currentIndex >= _count) {
                throw new NoSuchElementException();
            }

            return _items[_currentIndex];
        }

        /**
         * Сбрасывает перечислитель в исходное состояние, которое находится перед первым элементом в списке.
         */
        @Override
        public void reset() {
            _currentIndex = -1;
        }
    }
}