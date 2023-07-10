package delegates.generic;

@FunctionalInterface
public interface BiFunc<T1, T2, TResult> {
    TResult apply(T1 arg1, T2 arg2);
}
