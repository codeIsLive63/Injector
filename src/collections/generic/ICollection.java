package collections.generic;

public interface ICollection<T> extends IEnumerable<T> {
    int count();

    void add(T item);

    void clear();

    boolean contains(T item);

    void remove(T item);
}
