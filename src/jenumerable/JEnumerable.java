package jenumerable;

import collections.generic.IEnumerable;
import collections.generic.IEnumerator;
import collections.generic.List;
import delegates.generic.BiFunc;
import delegates.generic.Func;
import delegates.generic.Predicate;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

/**
 * Класс JEnumerable предоставляет возможности LINQ-подобных запросов для коллекций.
 * Этот класс реализует интерфейс IEnumerable
 *
 * @param <TSource> Тип элементов в коллекции.
 */
public class JEnumerable<TSource> implements IEnumerable<TSource> {

	private final IEnumerator<TSource> _enumerator;

	private JEnumerable(IEnumerator<TSource> enumerator) {
		_enumerator = enumerator;
	}

	/**
	 * Создает новый экземпляр JEnumerable из коллекции, реализующей
	 * Интерфейс IEnumerable.
	 *
	 * @param <TCollection> Тип элементов в коллекции.
	 * @param collection Коллекция, из которой создается JEnumerable.
	 * @return Новый экземпляр JEnumerable с элементами из входной коллекции.
	 */
	public static <TCollection> JEnumerable<TCollection> from(IEnumerable<TCollection> collection) {
		return new JEnumerable<>(collection.getEnumerator());
	}

	/**
	 * Создает новый экземпляр JEnumerable из массива.
	 *
	 * @param <TCollection> Тип элементов массива.
	 * @param collection Массив, из которого создается JEnumerable.
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
	 * @param collection Коллекция Iterable, из которой создается JEnumerable.
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
	 * @param selector Лямбда-выражение для преобразования каждого элемента.
	 * @return Новый экземпляр JEnumerable, содержащий преобразованные элементы.
	 */
	public <TResult> JEnumerable<TResult> select(Func<TSource, TResult> selector) {
		return new JEnumerable<>(new SelectIterator<>(_enumerator, selector));
	}

	/**
	 * Проецирует каждый элемент коллекции в новую форму, используя указанное
	 * лямбда-выражение, которое также включает индекс элемента.
	 *
	 * @param <TResult> Тип результирующих элементов.
	 * @param selector Лямбда-выражение для преобразования каждого элемента с его индексом.
	 * @return Новый экземпляр JEnumerable, содержащий преобразованные элементы.
	 */
	public <TResult> JEnumerable<TResult> select(BiFunc<TSource, Integer, TResult> selector) {
		return new JEnumerable<>(new SelectIterator<>(_enumerator, selector));
	}

	/**
	 * Производит проекцию каждого элемента списка на другой список элементов, с использованием
	 * указанного селектора и объединяет результаты в один список.
	 *
	 * @param selector Функция-селектор, которая принимает элемент типа TSource и возвращает
	 *                 перечисление элементов типа TResult.
	 * @param <TResult> Тип элементов результирующего списка.
	 * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селектора.
	 */
	public <TResult> JEnumerable<TResult> selectMany(Func<TSource, IEnumerable<TResult>> selector) {
		return new JEnumerable<>(new SelectManyIterator<>(_enumerator, selector));
	}

	/**
	 * Производит проекцию каждого элемента списка на другой список элементов, с использованием
	 * указанного селектора, передавая также индекс элемента, и объединяет результаты в один список.
	 *
	 * @param selector Функция-селектор, которая принимает элемент типа TSource и его индекс,
	 *                 и возвращает перечисление элементов типа TResult.
	 * @param <TResult> Тип элементов результирующего списка.
	 * @return Новый экземпляр JEnumerable, содержащий элементы из проекций с использованием селектора и индекса.
	 */
	public <TResult> JEnumerable<TResult> selectMany(BiFunc<TSource, Integer, IEnumerable<TResult>> selector) {
		return new JEnumerable<>(new SelectManyIndexedIterator<>(_enumerator, selector));
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
	public <TCollection, TResult> JEnumerable<TResult> selectMany(Func<TSource, IEnumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
		return new JEnumerable<>(new SelectManyResultIterator<>(_enumerator, collectionSelector, resultSelector));
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
	public <TCollection, TResult> JEnumerable<TResult> selectMany(BiFunc<TSource, Integer, IEnumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
		return new JEnumerable<>(new SelectManyIndexedResultIterator<>(_enumerator, collectionSelector, resultSelector));
	}

	/**
	 * Фильтрует элементы коллекции на основе указанного лямбда-выражения.
	 * <p>
	 * @param predicate Лямбда-выражение для проверки каждого элемента на наличие условия.
	 * @return Новый экземпляр JEnumerable, содержащий отфильтрованные элементы.
	 */
	public JEnumerable<TSource> where(Predicate<TSource> predicate) {
		return new JEnumerable<>(new WhereIterator<>(_enumerator, predicate));
	}

	/**
	 * Фильтрует элементы коллекции на основе указанного лямбда-выражения,
	 * которое также включает индекс элемента.
	 * <p>
	 * @param predicate Лямбда-выражение для проверки каждого элемента с его индексом на наличие условия.
	 * @return Новый экземпляр JEnumerable, содержащий отфильтрованные элементы.
	 */
	public JEnumerable<TSource> where(BiFunc<TSource, Integer, Boolean> predicate) {
		return new JEnumerable<>(new WhereIterator<>(_enumerator, predicate));
	}

	/**
	 * Подсчитывает общее количество элементов в последовательности.
	 *
	 * @return Количество элементов в последовательности.
	 */
	public int count() {
		int count = 0;

		while (_enumerator.moveNext()) {
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

		while (_enumerator.moveNext()) {
			if (predicate.apply(_enumerator.getCurrent())) {
				count++;
			}
		}

		return count;
	}

	public JEnumerable<TSource> take(int count) {
		return new JEnumerable<>(new TakeIterator<>(_enumerator, count));
	}

	/**
	 * Преобразует коллекцию JEnumerable в список.
	 *
	 * @return Список, содержащий элементы коллекции.
	 */
	public List<TSource> toList() {
		List<TSource> resultList = new List<>();

		while (_enumerator.moveNext()) {
			resultList.add(_enumerator.getCurrent());
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
		if (_enumerator.moveNext()) {
			TSource[] array = (TSource[]) Array.newInstance(_enumerator.getCurrent().getClass(), count());

			int index = 0;

			_enumerator.reset();
			while (_enumerator.moveNext()) {
				array[index++] = _enumerator.getCurrent();
			}

			return array;
		}

		return (TSource[]) new Object[] { };
	}

	@Override
	public IEnumerator<TSource> getEnumerator() {
		return _enumerator;
	}

	/**
	 * Класс SelectIterator — это итератор, который проецирует каждый элемент
	 * исходной коллекции в новую форму с использованием указанного лямбда-выражения.
	 *
	 * @param <TSource> Тип элементов исходной коллекции.
	 * @param <TResult> Тип элементов в результирующей проекции.
	 */
	private static class SelectIterator<TSource, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final Func<TSource, TResult> _selector;
		private final BiFunc<TSource, Integer, TResult> _indexedSelector;

		private int _index;
		private TResult _current;

		/**
		 * Инициализирует новый экземпляр класса SelectIterator с помощью лямбда-выражения.
		 *
		 * @param enumerator Исходный перечислитель.
		 * @param selector Лямбда-выражение для преобразования каждого элемента.
		 */
		private SelectIterator(IEnumerator<TSource> enumerator, Func<TSource, TResult> selector) {
			_enumerator = enumerator;
			_selector = selector;
			_indexedSelector = null;
			_index = -1;
		}

		/**
		 * Инициализирует новый экземпляр класса SelectIterator, используя указанное
		 * лямбда-выражение, которое также включает индекс элемента.
		 *
		 * @param enumerator Исходный перечислитель.
		 * @param selector Лямбда-выражение для преобразования каждого элемента с его индексом.
		 */
		private SelectIterator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, TResult> selector) {
			_enumerator = enumerator;
			_selector = null;
			_indexedSelector = selector;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			if (_enumerator.moveNext()) {
				_index++;
				if (_indexedSelector != null) {
					_current = _indexedSelector.apply(_enumerator.getCurrent(), _index);
				} else {
					if (_selector != null) {
						_current = _selector.apply(_enumerator.getCurrent());
					}
				}

				return true;
			}

			return false;
		}

		@Override
		public TResult getCurrent() {
			return _current;
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_index = -1;
			_current = null;
		}
	}

	/**
	 * Класс WhereIterator — это итератор, фильтрующий элементы исходной
	 * коллекции на основе указанного лямбда-выражения.
	 *
	 * @param <TSource> Тип элементов исходной коллекции.
	 */
	private static class WhereIterator<TSource> implements IEnumerator<TSource> {

		private final IEnumerator<TSource> _enumerator;
		private final Predicate<TSource> _predicate;
		private final BiFunc<TSource, Integer, Boolean> _indexedPredicate;
		private TSource _current;
		private int _index;

		/**
		 * Инициализирует новый экземпляр класса WhereIterator с помощью лямбда-выражения.
		 *
		 * @param enumerator Исходный перечислитель.
		 * @param predicate Лямбда-выражение для проверки каждого элемента на наличие условия.
		 */
		private WhereIterator(IEnumerator<TSource> enumerator, Predicate<TSource> predicate) {
			_enumerator = enumerator;
			_predicate = predicate;
			_indexedPredicate = null;
			_index = -1;
		}

		/**
		 * Инициализирует новый экземпляр класса WhereIterator с помощью лямбда-выражения,
		 * которое включает индекс элемента
		 *
		 * @param enumerator Исходный перечислитель.
		 * Предикат @param Лямбда-выражение для проверки каждого элемента с его индексом
		 * для условия.
		 */
		private WhereIterator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, Boolean> predicate) {
			_enumerator = enumerator;
			_predicate = null;
			_indexedPredicate = predicate;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			while (_enumerator.moveNext()) {
				_index++;
				_current = _enumerator.getCurrent();
				if (_predicate != null && _predicate.test(_current)) {
					return true;
				} else if (_indexedPredicate != null && _indexedPredicate.apply(_current, _index)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public TSource getCurrent() {
			return _current;
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_index = -1;
			_current = null;
		}
	}

	/**
	 * Внутренний класс, реализующий интерфейс IEnumerator для выполнения операции
	 * selectMany над элементами списка типа TSource.
	 *
	 * @param <TSource> Тип элементов исходного списка.
	 * @param <TResult> Тип элементов результирующего списка.
	 */
	private static class SelectManyIterator<TSource, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final Func<TSource, IEnumerable<TResult>> _selector;
		private IEnumerator<TResult> _currentEnumerator;

		/**
		 * Инициализирует новый экземпляр SelectManyIterator.
		 *
		 * @param enumerator Итератор исходного списка типа TSource.
		 * @param selector   Функция-селектор, которая принимает элемент типа TSource
		 *                   и возвращает перечисление элементов типа TResult.
		 */
		private SelectManyIterator(IEnumerator<TSource> enumerator, Func<TSource, IEnumerable<TResult>> selector) {
			_enumerator = enumerator;
			_selector = selector;
			_currentEnumerator = null;
		}

		@Override
		public boolean moveNext() {
			while (_currentEnumerator == null || !_currentEnumerator.moveNext()) {
				if (_enumerator.moveNext()) {
					_currentEnumerator = _selector.apply(_enumerator.getCurrent()).getEnumerator();
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public TResult getCurrent() {
			return _currentEnumerator.getCurrent();
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_currentEnumerator = null;
		}
	}

	/**
	 * Внутренний класс, реализующий интерфейс IEnumerator для выполнения операции
	 * selectMany над элементами списка типа TSource, с использованием индекса элементов
	 *
	 * @param <TSource>  Тип элементов исходного списка.
	 * @param <TResult>  Тип элементов результирующего списка.
	 */
	private static class SelectManyIndexedIterator<TSource, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final BiFunc<TSource, Integer, IEnumerable<TResult>> _selector;
		private IEnumerator<TResult> _currentEnumerator;
		private int _index;

		/**
		 * Инициализирует новый экземпляр класса SelectManyIndexedIterator.
		 *
		 * @param enumerator Итератор элементов исходного списка.
		 * @param selector   Функция-селектор, которая принимает элемент типа TSource и его индекс,
		 *                   и возвращает перечисление элементов типа TResult.
		 */
		private SelectManyIndexedIterator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, IEnumerable<TResult>> selector) {
			_enumerator = enumerator;
			_selector = selector;
			_currentEnumerator = null;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			while (_currentEnumerator == null || !_currentEnumerator.moveNext()) {
				if (_enumerator.moveNext()) {
					_index++;
					_currentEnumerator = _selector.apply(_enumerator.getCurrent(), _index).getEnumerator();
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public TResult getCurrent() {
			return _currentEnumerator.getCurrent();
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_currentEnumerator = null;
			_index = -1;
		}
	}

	/**
	 * Внутренний класс, реализующий интерфейс IEnumerator, который выполняет проекцию каждого элемента списка
	 * на другой список элементов с использованием указанного селектора, а затем объединяет результаты
	 * в один список с использованием указанного селектора результата.
	 *
	 * @param <TSource>       Тип элементов исходного списка.
	 * @param <TCollection>   Тип элементов промежуточного списка, полученного с использованием селектора.
	 * @param <TResult>       Тип элементов результирующего списка, полученного с использованием селектора результата.
	 */
	private static class SelectManyResultIterator<TSource, TCollection, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final Func<TSource, IEnumerable<TCollection>> _collectionSelector;
		private final BiFunc<TSource, TCollection, TResult> _resultSelector;
		private IEnumerator<TSource> _currentEnumerator;
		private IEnumerator<TCollection> _currentCollectionEnumerator;

		/**
		 * Инициализирует новый экземпляр класса SelectManyResultIterator.
		 *
		 * @param enumerator         Итератор исходного списка элементов типа TSource.
		 * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource
		 *                           и возвращает перечисление элементов типа TCollection.
		 * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource,
		 *                           элемент типа TCollection и возвращает элемент типа TResult.
		 */
		private SelectManyResultIterator(IEnumerator<TSource> enumerator, Func<TSource, IEnumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
			_enumerator = enumerator;
			_collectionSelector = collectionSelector;
			_resultSelector = resultSelector;
			_currentEnumerator = null;
			_currentCollectionEnumerator = null;
		}

		@Override
		public boolean moveNext() {
			while (_currentEnumerator == null || !_currentCollectionEnumerator.moveNext()) {
				if (_enumerator.moveNext()) {
					_currentEnumerator = _enumerator;
					_currentCollectionEnumerator = _collectionSelector.apply(_enumerator.getCurrent()).getEnumerator();
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public TResult getCurrent() {
			return _resultSelector.apply(_currentEnumerator.getCurrent(), _currentCollectionEnumerator.getCurrent());
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_currentEnumerator = null;
			_currentCollectionEnumerator = null;
		}
	}

	/**
	 * Внутренний класс, который реализует интерфейс IEnumerator для проекции каждого элемента списка
	 * на другой список элементов с использованием указанного селектора и индекса, а затем объединения
	 * результатов в один список с использованием указанного селектора результата.
	 *
	 * @param <TSource>         Тип исходных элементов списка.
	 * @param <TCollection>     Тип элементов промежуточного списка.
	 * @param <TResult>         Тип элементов результирующего списка.
	 */
	private static class SelectManyIndexedResultIterator<TSource, TCollection, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final BiFunc<TSource, Integer, IEnumerable<TCollection>> _collectionSelector;
		private final BiFunc<TSource, TCollection, TResult> _resultSelector;
		private IEnumerator<TSource> _currentEnumerator;
		private IEnumerator<TCollection> _currentCollectionEnumerator;
		private int _index;

		/**
		 * Инициализирует новый экземпляр класса SelectManyIndexedResultIterator.
		 *
		 * @param enumerator         Итератор исходного списка элементов типа TSource.
		 * @param collectionSelector Функция-селектор, которая принимает элемент типа TSource и его индекс,
		 *                           и возвращает перечисление элементов типа TCollection.
		 * @param resultSelector     Функция-селектор результата, которая принимает элемент типа TSource,
		 *                           элемент типа TCollection и индекс элемента, и возвращает элемент типа TResult.
		 */
		private SelectManyIndexedResultIterator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, IEnumerable<TCollection>> collectionSelector, BiFunc<TSource, TCollection, TResult> resultSelector) {
			_enumerator = enumerator;
			_collectionSelector = collectionSelector;
			_resultSelector = resultSelector;
			_currentEnumerator = null;
			_currentCollectionEnumerator = null;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			while (_currentEnumerator == null || !_currentCollectionEnumerator.moveNext()) {
				if (_enumerator.moveNext()) {
					_index++;
					_currentEnumerator = _enumerator;
					_currentCollectionEnumerator = _collectionSelector.apply(_enumerator.getCurrent(), _index).getEnumerator();
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public TResult getCurrent() {
			return _resultSelector.apply(_currentEnumerator.getCurrent(), _currentCollectionEnumerator.getCurrent());
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_currentEnumerator = null;
			_currentCollectionEnumerator = null;
			_index = -1;
		}
	}


	/**
	 * Класс, представляющий итератор для операции Take в JEnumerable.
	 *
	 * @param <TSource> 	Тип элементов в итераторе.
	 */
	private static class TakeIterator<TSource> implements IEnumerator<TSource> {

		private final IEnumerator<TSource> _enumerator;
		private final int _count;
		private int _currentIndex;

		/**
		 * Создает новый итератор TakeIterator.
		 *
		 * @param enumerator Итератор исходной последовательности.
		 * @param count      Количество элементов, которые нужно выбрать.
		 */
		public TakeIterator(IEnumerator<TSource> enumerator, int count) {
			_enumerator = enumerator;
			_count = count;
			_currentIndex = -1;
		}

		@Override
		public boolean moveNext() {
			if (_currentIndex < _count - 1 && _enumerator.moveNext()) {
				_currentIndex++;
				return true;
			}

			return false;
		}

		@Override
		public TSource getCurrent() {
			if (_currentIndex >= 0 && _currentIndex < _count) {
				return _enumerator.getCurrent();
			}

			throw new NoSuchElementException("No more elements");
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_currentIndex = -1;
		}
	}
}