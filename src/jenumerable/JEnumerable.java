package jenumerable;

import collections.generic.Enumerable;
import collections.generic.Enumerator;
import collections.generic.List;
import delegates.generic.BiFunc;
import delegates.generic.Func;
import delegates.generic.Predicate;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Класс JEnumerable предоставляет возможности LINQ-подобных запросов для коллекций.
 * Этот класс реализует интерфейс IEnumerable
 *
 * @param <TSource> Тип элементов в коллекции.
 */
public class JEnumerable<TSource> implements Enumerable<TSource> {

    private final Enumerator<TSource> enumerator;

    private JEnumerable(Enumerator<TSource> enumerator) {
        this.enumerator = enumerator;
    }

    /**
     * Создает новый экземпляр JEnumerable из коллекции, реализующей
     * Интерфейс IEnumerable.
     *
     * @param <TCollection> Тип элементов в коллекции.
     * @param collection    Коллекция, из которой создается JEnumerable.
     * @return Новый экземпляр JEnumerable с элементами из входной коллекции.
     */
    public static <TCollection> JEnumerable<TCollection> from(Enumerable<TCollection> collection) {
        return new JEnumerable<>(collection.getEnumerator());
    }

    /**
     * Создает новый экземпляр JEnumerable из массива.
     *
     * @param <TCollection> Тип элементов массива.
     * @param collection    Массив, из которого создается JEnumerable.
     * @return Новый экземпляр JEnumerable с элементами входного массива.
     */
    @SafeVarargs
    public static <TCollection> JEnumerable<TCollection> from(TCollection... collection) {
        return new JEnumerable<>(new List<>(collection).getEnumerator());
    }

    /**
     * Создает новый экземпляр JEnumerable из коллекции Iterable.
     *
     * @param <TCollection> Тип элементов в коллекции.
     * @param collection    Коллекция Iterable, из которой создается JEnumerable.
     * @return Новый экземпляр JEnumerable с элементами из входной коллекции.
     */
    public static <TCollection> JEnumerable<TCollection> from(Iterable<TCollection> collection) {
        return new JEnumerable<>(new List<>(collection).getEnumerator());
    }

    /**
     * Проецирует каждый элемент коллекции в новую форму, используя указанное
     * лямбда-выражение.
     *
     * @param <TResult> Тип результирующих элементов.
     * @param selector  Лямбда-выражение для преобразования каждого элемента.
     * @return Новый экземпляр JEnumerable, содержащий преобразованные элементы.
     */
    public <TResult> JEnumerable<TResult> select(Func<TSource, TResult> selector) {
        return new JEnumerable<>(new SelectIterator<>(enumerator, selector));
    }

    /**
     * Проецирует каждый элемент коллекции в новую форму, используя указанное
     * лямбда-выражение, которое также включает индекс элемента.
     *
     * @param <TResult> Тип результирующих элементов.
     * @param selector  Лямбда-выражение для преобразования каждого элемента с его индексом.
     * @return Новый экземпляр JEnumerable, содержащий преобразованные элементы.
     */
    public <TResult> JEnumerable<TResult> select(BiFunc<TSource, Integer, TResult> selector) {
        return new JEnumerable<>(new SelectIterator<>(enumerator, selector));
    }

    /**
     * Производит проекцию каждого элемента списка на другой список элементов, с использованием
     * указанного селектора и объединяет результаты в один список.
     *
     * @param selector  Функция-селектор, которая принимает элемент типа TSource и возвращает
     *                  перечисление элементов типа TResult.
     * @param <TResult> Тип элементов результирующего списка.
     * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селектора.
     */
    public <TResult> JEnumerable<TResult> selectMany(Func<TSource, Enumerable<TResult>> selector) {
        return new JEnumerable<>(new SelectManyIterator<>(enumerator, selector));
    }

    /**
     * Производит проекцию каждого элемента списка на другой список элементов, с использованием
     * указанного селектора, передавая также индекс элемента, и объединяет результаты в один список.
     *
     * @param selector  Функция-селектор, которая принимает элемент типа TSource и его индекс,
     *                  и возвращает перечисление элементов типа TResult.
     * @param <TResult> Тип элементов результирующего списка.
     * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селектора и индекса.
     */
    public <TResult> JEnumerable<TResult> selectMany(BiFunc<TSource, Integer, Enumerable<TResult>> selector) {
        return new JEnumerable<>(new SelectManyIndexedIterator<>(enumerator, selector));
    }

    /**
     * Производит проекцию каждого элемента списка на другой список элементов с использованием
     * указанного селектора, а затем объединяет результаты в один список с использованием
     * указанного селектора результата.
     *
     * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource и возвращает
     *                           перечисление элементов типа TCollection.
     * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource
     *                           и элемент типа TCollection, и возвращает элемент типа TResult.
     * @param <TCollection>      Тип элементов промежуточного списка.
     * @param <TResult>          Тип элементов результирующего списка.
     * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селекторов.
     */
    public <TCollection, TResult> JEnumerable<TResult> selectMany(Func<TSource, Enumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
        return new JEnumerable<>(new SelectManyResultIterator<>(enumerator, collectionSelector, resultSelector));
    }

    /**
     * Производит проекцию каждого элемента списка на другой список элементов, с использованием
     * указанного селектора и индекса, а затем объединяет результаты в один список с использованием
     * указанного селектора результата.
     *
     * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource и его индекс,
     *                           и возвращает перечисление элементов типа TCollection.
     * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource,
     *                           элемент типа TCollection и индекс элемента, и возвращает элемент типа TResult.
     * @param <TCollection>      Тип элементов промежуточного списка.
     * @param <TResult>          Тип элементов результирующего списка.
     * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селекторов и индекса.
     */
    public <TCollection, TResult> JEnumerable<TResult> selectMany(BiFunc<TSource, Integer, Enumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
        return new JEnumerable<>(new SelectManyIndexedResultIterator<>(enumerator, collectionSelector, resultSelector));
    }

    /**
     * Фильтрует элементы коллекции на основе указанного лямбда-выражения.
     * <p>
     *
     * @param predicate Лямбда-выражение для проверки каждого элемента на наличие условия.
     * @return Новый экземпляр JEnumerable, содержащий отфильтрованные элементы.
     */
    public JEnumerable<TSource> where(Predicate<TSource> predicate) {
        return new JEnumerable<>(new WhereIterator<>(enumerator, predicate));
    }

    /**
     * Фильтрует элементы коллекции на основе указанного лямбда-выражения,
     * которое также включает индекс элемента.
     * <p>
     *
     * @param predicate Лямбда-выражение для проверки каждого элемента с его индексом на наличие условия.
     * @return Новый экземпляр JEnumerable, содержащий отфильтрованные элементы.
     */
    public JEnumerable<TSource> where(BiFunc<TSource, Integer, Boolean> predicate) {
        return new JEnumerable<>(new WhereIterator<>(enumerator, predicate));
    }

    /**
     * Подсчитывает общее количество элементов в последовательности.
     *
     * @return Количество элементов в последовательности.
     */
    public int count() {
        int count = 0;

        while (enumerator.moveNext()) {
            count++;
        }

        return count;
    }

    /**
     * Подсчитывает количество элементов в последовательности, которые удовлетворяют заданному условию.
     *
     * @param predicate Функция-предикат, которая определяет условие для подсчета элементов.
     * @return Количество элементов, удовлетворяющих заданному условию.
     */
    public int count(Func<TSource, Boolean> predicate) {
        int count = 0;

        while (enumerator.moveNext()) {
            if (predicate.apply(enumerator.getCurrent())) {
                count++;
            }
        }

        return count;
    }

    /**
     * Возвращает новую коллекцию, содержащую указанное количество элементов из начала текущей последовательности.
     *
     * @param count Количество элементов для возврата из начала текущей последовательности.
     * @return Новый экземпляр JEnumerable, содержащиё первые {@code count} элементов текущей последовательности.
     */
    public JEnumerable<TSource> take(int count) {
        return new JEnumerable<>(new TakeIterator<>(enumerator, count));
    }

    /**
     * Преобразует коллекцию JEnumerable в список.
     *
     * @return Список, содержащий элементы коллекции.
     */
    public List<TSource> toList() {
        List<TSource> resultList = new List<>();

        while (enumerator.moveNext()) {
            resultList.add(enumerator.getCurrent());
        }

        return resultList;
    }

    /**
     * Преобразует коллекцию JEnumerable в массив.
     *
     * @return Массив, содержащий элементы коллекции.
     */
    @SuppressWarnings("unchecked")
    public TSource[] toArray() {
        if (enumerator.moveNext()) {
            TSource[] array = (TSource[]) Array.newInstance(enumerator.getCurrent().getClass(), count());

            int index = 0;

            enumerator.reset();
            while (enumerator.moveNext()) {
                array[index++] = enumerator.getCurrent();
            }

            return array;
        }

        return (TSource[]) new Object[]{};
    }

    /**
     * Преобразует коллекцию JEnumerable в последовательный поток элементов.
     *
     * @return Последовательный поток элементов коллекции.
     */
    public Stream<TSource> asStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(enumerator.asIterator(), Spliterator.ORDERED), false);
    }

    /**
     * Преобразует коллекцию JEnumerable в параллельный поток элементов.
     *
     * @return Параллельный поток элементов коллекции.
     */
    public Stream<TSource> asParallelStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(enumerator.asIterator(), Spliterator.ORDERED), true);
    }

    /**
     * Проверяет, содержит ли коллекция хотя бы один элемент, удовлетворяющий заданному условию.
     *
     * @param predicate Лямбда-выражение для проверки каждого элемента.
     * @return true, если коллекция содержит хотя бы один элемент, удовлетворяющий условию; иначе false.
     */
    public boolean any(Predicate<TSource> predicate) {
        while (enumerator.moveNext()) {
            if (predicate.test(enumerator.getCurrent())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, удовлетворяют ли все элементы в последовательности указанному предикату.
     *
     * @param predicate Предикат для проверки каждого элемента.
     * @return true, если все элементы удовлетворяют предикату; иначе false.
     */
    public boolean all(Predicate<TSource> predicate) {
        while (enumerator.moveNext()) {
            if (!predicate.test(enumerator.getCurrent())) {
                return false;
            }
        }

        return true;
    }


    @Override
    public Enumerator<TSource> getEnumerator() {
        return enumerator;
    }

    /**
     * Класс SelectIterator — это итератор, который проецирует каждый элемент
     * исходной коллекции в новую форму с использованием указанного лямбда-выражения.
     *
     * @param <TSource> Тип элементов исходной коллекции.
     * @param <TResult> Тип элементов в результирующей проекции.
     */
    private static class SelectIterator<TSource, TResult> implements Enumerator<TResult> {

        private final Enumerator<TSource> enumerator;
        private final Func<TSource, TResult> selector;
        private final BiFunc<TSource, Integer, TResult> indexedSelector;

        private int index;
        private TResult current;

        /**
         * Инициализирует новый экземпляр класса SelectIterator с помощью лямбда-выражения.
         *
         * @param enumerator Исходный перечислитель.
         * @param selector   Лямбда-выражение для преобразования каждого элемента.
         */
        private SelectIterator(Enumerator<TSource> enumerator, Func<TSource, TResult> selector) {
            this.enumerator = enumerator;
            this.selector = selector;
            indexedSelector = null;
            index = -1;
        }

        /**
         * Инициализирует новый экземпляр класса SelectIterator, используя указанное
         * лямбда-выражение, которое также включает индекс элемента.
         *
         * @param enumerator Исходный перечислитель.
         * @param selector   Лямбда-выражение для преобразования каждого элемента с его индексом.
         */
        private SelectIterator(Enumerator<TSource> enumerator, BiFunc<TSource, Integer, TResult> selector) {
            this.enumerator = enumerator;
            this.selector = null;
            indexedSelector = selector;
            index = -1;
        }

        @Override
        public boolean moveNext() {
            if (enumerator.moveNext()) {
                index++;
                if (indexedSelector != null) {
                    current = indexedSelector.apply(enumerator.getCurrent(), index);
                } else {
                    if (selector != null) {
                        current = selector.apply(enumerator.getCurrent());
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public TResult getCurrent() {
            return current;
        }

        @Override
        public void reset() {
            enumerator.reset();
            index = -1;
            current = null;
        }
    }

    /**
     * Класс WhereIterator — это итератор, фильтрующий элементы исходной
     * коллекции на основе указанного лямбда-выражения.
     *
     * @param <TSource> Тип элементов исходной коллекции.
     */
    private static class WhereIterator<TSource> implements Enumerator<TSource> {

        private final Enumerator<TSource> enumerator;
        private final Predicate<TSource> predicate;
        private final BiFunc<TSource, Integer, Boolean> indexedPredicate;
        private TSource current;
        private int index;

        /**
         * Инициализирует новый экземпляр класса WhereIterator с помощью лямбда-выражения.
         *
         * @param enumerator Исходный перечислитель.
         * @param predicate  Лямбда-выражение для проверки каждого элемента на наличие условия.
         */
        private WhereIterator(Enumerator<TSource> enumerator, Predicate<TSource> predicate) {
            this.enumerator = enumerator;
            this.predicate = predicate;
            indexedPredicate = null;
            index = -1;
        }

        /**
         * Инициализирует новый экземпляр класса WhereIterator с помощью лямбда-выражения,
         * которое включает индекс элемента
         *
         * @param enumerator Исходный перечислитель.
         *                   Предикат @param Лямбда-выражение для проверки каждого элемента с его индексом
         *                   для условия.
         */
        private WhereIterator(Enumerator<TSource> enumerator, BiFunc<TSource, Integer, Boolean> predicate) {
            this.enumerator = enumerator;
            this.predicate = null;
            indexedPredicate = predicate;
            index = -1;
        }

        @Override
        public boolean moveNext() {
            while (enumerator.moveNext()) {
                index++;
                current = enumerator.getCurrent();
                if (predicate != null && predicate.test(current)) {
                    return true;
                } else if (indexedPredicate != null && indexedPredicate.apply(current, index)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public TSource getCurrent() {
            return current;
        }

        @Override
        public void reset() {
            enumerator.reset();
            index = -1;
            current = null;
        }
    }

    /**
     * Внутренний класс, реализующий интерфейс IEnumerator для выполнения операции
     * selectMany над элементами списка типа TSource.
     *
     * @param <TSource> Тип элементов исходного списка.
     * @param <TResult> Тип элементов результирующего списка.
     */
    private static class SelectManyIterator<TSource, TResult> implements Enumerator<TResult> {

        private final Enumerator<TSource> enumerator;
        private final Func<TSource, Enumerable<TResult>> selector;
        private Enumerator<TResult> currentEnumerator;

        /**
         * Инициализирует новый экземпляр SelectManyIterator.
         *
         * @param enumerator Итератор исходного списка типа TSource.
         * @param selector   Функция-селектор, которая принимает элемент типа TSource
         *                   и возвращает перечисление элементов типа TResult.
         */
        private SelectManyIterator(Enumerator<TSource> enumerator, Func<TSource, Enumerable<TResult>> selector) {
            this.enumerator = enumerator;
            this.selector = selector;
            currentEnumerator = null;
        }

        @Override
        public boolean moveNext() {
            while (currentEnumerator == null || !currentEnumerator.moveNext()) {
                if (enumerator.moveNext()) {
                    currentEnumerator = selector.apply(enumerator.getCurrent()).getEnumerator();
                } else {
                    return false;
                }
            }

            return true;
        }

        @Override
        public TResult getCurrent() {
            return currentEnumerator.getCurrent();
        }

        @Override
        public void reset() {
            enumerator.reset();
            currentEnumerator = null;
        }
    }

    /**
     * Внутренний класс, реализующий интерфейс IEnumerator для выполнения операции
     * selectMany над элементами списка типа TSource, с использованием индекса элементов
     *
     * @param <TSource> Тип элементов исходного списка.
     * @param <TResult> Тип элементов результирующего списка.
     */
    private static class SelectManyIndexedIterator<TSource, TResult> implements Enumerator<TResult> {

        private final Enumerator<TSource> enumerator;
        private final BiFunc<TSource, Integer, Enumerable<TResult>> selector;
        private Enumerator<TResult> currentEnumerator;
        private int index;

        /**
         * Инициализирует новый экземпляр класса SelectManyIndexedIterator.
         *
         * @param enumerator Итератор элементов исходного списка.
         * @param selector   Функция-селектор, которая принимает элемент типа TSource и его индекс,
         *                   и возвращает перечисление элементов типа TResult.
         */
        private SelectManyIndexedIterator(Enumerator<TSource> enumerator, BiFunc<TSource, Integer, Enumerable<TResult>> selector) {
            this.enumerator = enumerator;
            this.selector = selector;
            currentEnumerator = null;
            index = -1;
        }

        @Override
        public boolean moveNext() {
            while (currentEnumerator == null || !currentEnumerator.moveNext()) {
                if (enumerator.moveNext()) {
                    index++;
                    currentEnumerator = selector.apply(enumerator.getCurrent(), index).getEnumerator();
                } else {
                    return false;
                }
            }

            return true;
        }

        @Override
        public TResult getCurrent() {
            return currentEnumerator.getCurrent();
        }

        @Override
        public void reset() {
            enumerator.reset();
            currentEnumerator = null;
            index = -1;
        }
    }

    /**
     * Внутренний класс, реализующий интерфейс IEnumerator, который выполняет проекцию каждого элемента списка
     * на другой список элементов с использованием указанного селектора, а затем объединяет результаты
     * в один список с использованием указанного селектора результата.
     *
     * @param <TSource>     Тип элементов исходного списка.
     * @param <TCollection> Тип элементов промежуточного списка, полученного с использованием селектора.
     * @param <TResult>     Тип элементов результирующего списка, полученного с использованием селектора результата.
     */
    private static class SelectManyResultIterator<TSource, TCollection, TResult> implements Enumerator<TResult> {

        private final Enumerator<TSource> enumerator;
        private final Func<TSource, Enumerable<TCollection>> collectionSelector;
        private final BiFunc<TSource, TCollection, TResult> resultSelector;
        private Enumerator<TSource> currentEnumerator;
        private Enumerator<TCollection> currentCollectionEnumerator;

        /**
         * Инициализирует новый экземпляр класса SelectManyResultIterator.
         *
         * @param enumerator         Итератор исходного списка элементов типа TSource.
         * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource
         *                           и возвращает перечисление элементов типа TCollection.
         * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource,
         *                           элемент типа TCollection и возвращает элемент типа TResult.
         */
        private SelectManyResultIterator(Enumerator<TSource> enumerator, Func<TSource, Enumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
            this.enumerator = enumerator;
            this.collectionSelector = collectionSelector;
            this.resultSelector = resultSelector;
            currentEnumerator = null;
            currentCollectionEnumerator = null;
        }

        @Override
        public boolean moveNext() {
            while (currentEnumerator == null || !currentCollectionEnumerator.moveNext()) {
                if (enumerator.moveNext()) {
                    currentEnumerator = enumerator;
                    currentCollectionEnumerator = collectionSelector.apply(enumerator.getCurrent()).getEnumerator();
                } else {
                    return false;
                }
            }

            return true;
        }

        @Override
        public TResult getCurrent() {
            return resultSelector.apply(currentEnumerator.getCurrent(), currentCollectionEnumerator.getCurrent());
        }

        @Override
        public void reset() {
            enumerator.reset();
            currentEnumerator = null;
            currentCollectionEnumerator = null;
        }
    }

    /**
     * Внутренний класс, который реализует интерфейс IEnumerator для проекции каждого элемента списка
     * на другой список элементов с использованием указанного селектора и индекса, а затем объединения
     * результатов в один список с использованием указанного селектора результата.
     *
     * @param <TSource>     Тип исходных элементов списка.
     * @param <TCollection> Тип элементов промежуточного списка.
     * @param <TResult>     Тип элементов результирующего списка.
     */
    private static class SelectManyIndexedResultIterator<TSource, TCollection, TResult> implements Enumerator<TResult> {

        private final Enumerator<TSource> enumerator;
        private final BiFunc<TSource, Integer, Enumerable<TCollection>> collectionSelector;
        private final BiFunc<TSource, TCollection, TResult> resultSelector;
        private Enumerator<TSource> currentEnumerator;
        private Enumerator<TCollection> currentCollectionEnumerator;
        private int index;

        /**
         * Инициализирует новый экземпляр класса SelectManyIndexedResultIterator.
         *
         * @param enumerator         Итератор исходного списка элементов типа TSource.
         * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource и его индекс,
         *                           и возвращает перечисление элементов типа TCollection.
         * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource,
         *                           элемент типа TCollection и индекс элемента, и возвращает элемент типа TResult.
         */
        private SelectManyIndexedResultIterator(Enumerator<TSource> enumerator, BiFunc<TSource, Integer, Enumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
            this.enumerator = enumerator;
            this.collectionSelector = collectionSelector;
            this.resultSelector = resultSelector;
            currentEnumerator = null;
            currentCollectionEnumerator = null;
            index = -1;
        }

        @Override
        public boolean moveNext() {
            while (currentEnumerator == null || !currentCollectionEnumerator.moveNext()) {
                if (enumerator.moveNext()) {
                    index++;
                    currentEnumerator = enumerator;
                    currentCollectionEnumerator = collectionSelector.apply(enumerator.getCurrent(), index).getEnumerator();
                } else {
                    return false;
                }
            }

            return true;
        }

        @Override
        public TResult getCurrent() {
            return resultSelector.apply(currentEnumerator.getCurrent(), currentCollectionEnumerator.getCurrent());
        }

        @Override
        public void reset() {
            enumerator.reset();
            currentEnumerator = null;
            currentCollectionEnumerator = null;
            index = -1;
        }
    }

    /**
     * Класс, представляющий итератор для операции Take в JEnumerable.
     *
     * @param <TSource> Тип элементов в итераторе.
     */
    private static class TakeIterator<TSource> implements Enumerator<TSource> {

        private final Enumerator<TSource> enumerator;
        private final int count;
        private int currentIndex;

        /**
         * Создает новый итератор TakeIterator.
         *
         * @param enumerator Итератор исходной последовательности.
         * @param count      Количество элементов, которые нужно выбрать.
         */
        public TakeIterator(Enumerator<TSource> enumerator, int count) {
            this.enumerator = enumerator;
            this.count = count;
            currentIndex = -1;
        }

        @Override
        public boolean moveNext() {
            if (currentIndex < count - 1 && enumerator.moveNext()) {
                currentIndex++;
                return true;
            }

            return false;
        }

        @Override
        public TSource getCurrent() {
            if (currentIndex >= 0 && currentIndex < count) {
                return enumerator.getCurrent();
            }

            throw new NoSuchElementException("Больше нету элементов");
        }

        @Override
        public void reset() {
            enumerator.reset();
            currentIndex = -1;
        }
    }
}