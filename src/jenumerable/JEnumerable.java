package jenumerable;

import collections.generic.IEnumerable;
import collections.generic.IEnumerator;
import collections.generic.List;
import delegates.generic.BiFunc;
import delegates.generic.Func;
import delegates.generic.Predicate;

import java.lang.reflect.Array;

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
	public static <TCollection> JEnumerable<TCollection> from(TCollection[] collection) {
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
		List<TSource> list = toList();
		TSource[] array = (TSource[]) Array.newInstance(list.get(0).getClass(), list.count());

		for (int i = 0; i < list.count(); i++) {
			array[i] = list.get(i);
		}

		return array;
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
			_index = -1;
			_indexedPredicate = predicate;
		}

		@Override
		public boolean moveNext() {
			while (_enumerator.moveNext()) {
				_index++;
				_current = _enumerator.getCurrent();
				if (_predicate != null && _predicate.test(_current)) {
					return true;
				}
				if (_indexedPredicate != null && _indexedPredicate.apply(_current, _index)) {
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
}