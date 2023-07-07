package collections.generic;

import java.util.NoSuchElementException;

public class List<T> implements IList<T>, ICollection<T>, IEnumerable<T> {

    private T[] _items;

    private int _count;

    public List(T[] collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Коллекция не должна быть пустой");
        }

        _items = collection;
        _count = collection.length;
    }

    @SuppressWarnings("unchecked")
    public List(Iterable<T> collection) {
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

    @Override
    public IEnumerator<T> getEnumerator() {
        return new ListEnumerator();
    }

    @Override
    public int count() {
        return _count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(T item) {
        T[] newItems = (T[]) new Object[_count + 1];

        System.arraycopy(_items, 0, newItems, 0, _count);
        newItems[_count] = item;

        _items = newItems;
        _count++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        _count = 0;
        _items = (T[]) new Object[0];
    }

    @Override
    public boolean contains(T item) {
        for (int i = 0; i < _count; i++)
            if(_items[i].equals(item))
                return true;

        return false;
    }

    @Override
    public void remove(T item) {
        int index = indexOf(item);

        if(index >= 0)
            removeAt(index);
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= _count)
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");

        return _items[index];
    }

    @Override
    public void set(int index, T item) {
        if(index < 0 || index >= _count)
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");

        _items[index] = item;
    }

    @Override
    public int indexOf(T item) {
        for (int i = 0; i < _count; i++)
            if(_items[i].equals(item))
                return i;

        return -1;
    }

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

    private class ListEnumerator implements IEnumerator<T> {

        private int _currentIndex;

        public ListEnumerator() {
            _currentIndex = -1;
        }

        @Override
        public boolean moveNext() {
            _currentIndex++;
            return _currentIndex < _count;
        }

        @Override
        public T getCurrent() {
            if (_currentIndex < 0 || _currentIndex >= _count) {
                throw new NoSuchElementException();
            }

            return _items[_currentIndex];
        }

        @Override
        public void reset() {
            _currentIndex = -1;
        }
    }
}