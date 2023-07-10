package jinq;

import collections.generic.IEnumerable;
import collections.generic.List;
import delegates.generic.BiFunc;
import delegates.generic.Func;
import delegates.generic.Predicate;

public class JStream<T> {

    private final IEnumerable<T> _source;

    private JStream(IEnumerable<T> source) {
        _source = source;
    }

    public static <TCollection> JStream<TCollection> entry(IEnumerable<TCollection> collection) {
        return new JStream<>(new List<>(collection));
    }

    public static <TCollection> JStream<TCollection> entry(TCollection[] collection) {
        return new JStream<>(new List<>(collection));
    }

    public static <TCollection> JStream<TCollection> entry(Iterable<TCollection> collection) {
        return new JStream<>(new List<>(collection));
    }

    public JStream<T> where(Predicate<T> predicate) {
        List<T> filteredList = new List<>();

        for (var item : _source) {
            if(predicate.test(item)) {
                filteredList.add(item);
            }
        }

        return new JStream<>(filteredList);
    }

    public <TResult> JStream<TResult> select(Func<T, TResult> selector) {
        List<TResult> selectedList = new List<>();

        for (var item : _source) {
            TResult result = selector.apply(item);
            selectedList.add(result);
        }

        return new JStream<>(selectedList);
    }

    public <TResult> JStream<TResult> select(BiFunc<T, Integer, TResult> selector) {
        List<TResult> selectedList = new List<>();

        int index = 0;

        for (var item : _source) {
            TResult result = selector.apply(item, index);
            selectedList.add(result);
            index++;
        }

        return new JStream<>(selectedList);
    }

    public List<T> toList() {
        return new List<>(_source);
    }
}