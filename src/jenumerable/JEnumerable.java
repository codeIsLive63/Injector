package jenumerable;

import collections.generic.IEnumerable;
import collections.generic.IEnumerator;
import collections.generic.List;
import delegates.generic.BiFunc;
import delegates.generic.Func;
import delegates.generic.Predicate;

public class JEnumerable<TSource> implements IEnumerable<TSource> {

	private final IEnumerator<TSource> _enumerator;

	private JEnumerable(IEnumerator<TSource> enumerator) {
		_enumerator = enumerator;
	}

	public static <TCollection> JEnumerable<TCollection> from(IEnumerable<TCollection> collection) {
		return new JEnumerable<>(collection.getEnumerator());
	}

	public static <TCollection> JEnumerable<TCollection> from(TCollection[] collection) {
		return new JEnumerable<>(new List<>(collection).getEnumerator());
	}

	public static <TCollection> JEnumerable<TCollection> from(Iterable<TCollection> collection) {
		return new JEnumerable<>(new List<>(collection).getEnumerator());
	}

	public <TResult> JEnumerable<TResult> select(Func<TSource, TResult> selector) {
		return new JEnumerable<>(new SelectEnumerator<>(_enumerator, selector));
	}

	public <TResult> JEnumerable<TResult> select(BiFunc<TSource, Integer, TResult> selector) {
		return new JEnumerable<>(new SelectIndexedEnumerator<>(_enumerator, selector));
	}

	public JEnumerable<TSource> where(Predicate<TSource> predicate) {
		return new JEnumerable<>(new WhereIterator<>(_enumerator, predicate));
	}

	public JEnumerable<TSource> where(BiFunc<TSource, Integer, Boolean> predicate) {
		return new JEnumerable<>(new WhereIndexedIterator<>(_enumerator, predicate));
	}

	public List<TSource> toList() {
		List<TSource> resultList = new List<>();

		while (_enumerator.moveNext()) {
			resultList.add(_enumerator.getCurrent());
		}

		return resultList;
	}

	@Override
	public IEnumerator<TSource> getEnumerator() {
		return _enumerator;
	}

	private static class SelectEnumerator<TSource, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final Func<TSource, TResult> _selector;

		private SelectEnumerator(IEnumerator<TSource> enumerator, Func<TSource, TResult> selector) {
			_enumerator = enumerator;
			_selector = selector;
		}

		@Override
		public boolean moveNext() {
			return _enumerator.moveNext();
		}

		@Override
		public TResult getCurrent() {
			return _selector.apply(_enumerator.getCurrent());
		}

		@Override
		public void reset() {
			_enumerator.reset();
		}
	}

	private static class SelectIndexedEnumerator<TSource, TResult> implements IEnumerator<TResult> {

		private final IEnumerator<TSource> _enumerator;
		private final BiFunc<TSource, Integer, TResult> _selector;
		private int _index;

		private SelectIndexedEnumerator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, TResult> selector) {
			_enumerator = enumerator;
			_selector = selector;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			if (_enumerator.moveNext()) {
				_index++;
				return true;
			}

			return false;
		}

		@Override
		public TResult getCurrent() {
			return _selector.apply(_enumerator.getCurrent(), _index);
		}

		@Override
		public void reset() {
			_enumerator.reset();
			_index = -1;
		}
	}

	private static class WhereIterator<TSource> implements IEnumerator<TSource> {

		private final IEnumerator<TSource> _enumerator;
		private final Predicate<TSource> _predicate;
		private TSource _current;

		private WhereIterator(IEnumerator<TSource> enumerator, Predicate<TSource> predicate) {
			_enumerator = enumerator;
			_predicate = predicate;
		}

		@Override
		public boolean moveNext() {
			while (_enumerator.moveNext()) {
				_current = _enumerator.getCurrent();
				if (_predicate.test(_current)) {
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
			_current = null;
		}
	}

	private static class WhereIndexedIterator<TSource> implements IEnumerator<TSource> {

		private final IEnumerator<TSource> _enumerator;
		private final BiFunc<TSource, Integer, Boolean> _predicate;
		private TSource _current;
		private int _index;

		private WhereIndexedIterator(IEnumerator<TSource> enumerator, BiFunc<TSource, Integer, Boolean> predicate) {
			_enumerator = enumerator;
			_predicate = predicate;
			_index = -1;
		}

		@Override
		public boolean moveNext() {
			while (_enumerator.moveNext()) {
				_index++;
				_current = _enumerator.getCurrent();
				if (_predicate.apply(_current, _index)) {
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