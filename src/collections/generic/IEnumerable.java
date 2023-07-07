package collections.generic;

import java.util.Iterator;

public interface IEnumerable<T> extends Iterable<T> {
    IEnumerator<T> getEnumerator();

    default Iterator<T> iterator() {
        return getEnumerator().asIterator();
    }
}
