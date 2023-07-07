import collections.generic.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new List<>();

        list.add(1);
        list.add(2);
        list.add(3);

        list.forEach(System.out::print);

        for (Integer integer : list) {
            System.out.println(integer);
        }
    }
}