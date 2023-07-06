package collections.generic;

public interface IList<T> extends ICollection<T>, IEnumerable<T> {
    T get(int index);

    void set(int index, T item);

    int indexOf(T item);

    void removeAt(int index);
}
