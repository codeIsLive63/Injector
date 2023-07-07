package collections.generic;

import java.util.Iterator;

public interface IEnumerator<T> {
    boolean moveNext();

    T getCurrent();

    void reset();

    default Iterator<T> asIterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return moveNext();
            }

            @Override
            public T next() {
                return getCurrent();
            }
        };
    }
}
