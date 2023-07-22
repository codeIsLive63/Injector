package delegates.generic;

@FunctionalInterface
public interface AnyFunc<TResult> {
    TResult apply();
}