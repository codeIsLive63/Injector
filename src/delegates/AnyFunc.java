package delegates;

@FunctionalInterface
public interface AnyFunc<TResult> {
    TResult apply();
}