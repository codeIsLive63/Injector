import collections.generic.List;
import jenumerable.JEnumerable;

public class Main {
    public static void main(String[] args) {
        List<Person> personList = new List<>(new Person[]{
                new Person("Tom", 19),
                new Person("Nikita", 18),
                new Person("Andrey", 20),
                new Person("Seele", 24),
                new Person("Gepard", 25),
                new Person("Arlan", 21),
                new Person("Alex", 34)
        });


        var result = JEnumerable.from(personList)
                        .where(p -> p.getName().equals("Alex"))
                        .select(Person::getAge)
                        .toArray();

        System.out.println(result[0]);
    }
}

class Person {
    private final String _name;

    private final int _age;

    public Person(String name, int age) {
        _name = name;
        _age = age;
    }

    public String getName() {
        return _name;
    }

    public int getAge() {
        return _age;
    }
}