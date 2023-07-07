package delegates.generic;

@FunctionalInterface
public interface Func<T1, TResult> {
    TResult apply(T1 arg);
}