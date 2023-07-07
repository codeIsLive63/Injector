package delegates.generic;

@FunctionalInterface
public interface Action<T> {
    void invoke(T arg);
}