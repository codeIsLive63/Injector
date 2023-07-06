package collections.generic;

import java.util.NoSuchElementException;

public class List<T> implements IList<T>, ICollection<T>, IEnumerable<T> {

    private T[] _items;

    private int _size;

    public List(T[] collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Коллекция не должна быть пустой");
        }

        _items = collection;
        _size = collection.length;
    }

    @SuppressWarnings("unchecked")
    public List(Iterable<T> collection) {
        _size = 0;
        for (T ignored : collection) {
            _size++;
        }

        if (_size == 0) {
            _items = (T[]) new Object[0];
            return;
        }

        _items = (T[]) new Object[_size];

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
    public int size() {
        return _size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(T item) {
        T[] newItems = (T[]) new Object[_size + 1];

        System.arraycopy(_items, 0, newItems, 0, _size);
        newItems[_size] = item;

        _items = newItems;
        _size++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        _size = 0;
        _items = (T[]) new Object[0];
    }

    @Override
    public boolean contains(T item) {
        for (int i = 0; i < _size; i++)
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
        if (index < 0 || index >= _size)
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");

        return _items[index];
    }

    @Override
    public void set(int index, T item) {
        if(index < 0 || index >= _size)
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");

        _items[index] = item;
    }

    @Override
    public int indexOf(T item) {
        for (int i = 0; i < _size; i++)
            if(_items[i].equals(item))
                return i;

        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeAt(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException("Индекс находится вне диапазона");
        }

        T[] newItems = (T[]) new Object[_size - 1];

        System.arraycopy(_items, 0, newItems, 0, index);
        System.arraycopy(_items, index + 1, newItems, index, _size - index - 1);

        _items = newItems;
        _size--;
    }

    private class ListEnumerator implements IEnumerator<T> {

        private int _currentIndex;

        public ListEnumerator() {
            _currentIndex = -1;
        }

        @Override
        public boolean hasNext() {
            return _currentIndex < _size - 1;
        }

        @Override
        public T next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            _currentIndex++;
            return _items[_currentIndex];
        }
    }
}