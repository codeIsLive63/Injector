package delegates.generic;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T arg);
}