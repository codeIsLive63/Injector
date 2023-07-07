import collections.generic.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> ints = new List<>(new Integer[]{1, 2, 3, 4});

        for (var i : ints) {
            System.out.println(i);
        }
    }
}