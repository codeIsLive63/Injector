import collections.generic.List;
import jenumerable.JEnumerable;

public class Main {
    public static void main(String[] args) {
        //Многомерный список для примера №1
        List<List<List<Integer>>> nestedList = new List<>() {{
           add(new List<>(){{
               add(new List<>(1, 2, 3, 4));
           }});

           add(new List<>(){{
               add(new List<>(5, 6, 7));
           }});

           add(new List<>() {{
               add(new List<>(8, 9, 10));
           }});
        }};

        //Пример №1. Для уменьшения размерности коллекции и объединения всех элементов в 1 список
        JEnumerable.from(nestedList)
                .selectMany(list -> JEnumerable.from(list).selectMany(innerList -> innerList))
                .forEach(e -> System.out.print(e + " "));
        System.out.println("\n");

        //Список для следующих примеров
        List<Person> peoples = new List<>(
                new Person("Nikita", 19, "C#", "JavaScript", "Java", "C++"),
                new Person("Alexey", 26, "Go", "C", "Ruby", "Python"),
                new Person("Andrey", 20, "Python", "PHP", "Assembler")
        );

        //Пример №2. Для объединения всех элементов списка в один список
        JEnumerable.from(peoples)
                .selectMany(Person::getFavoriteLanguages)
                .forEach(System.out::println);

        System.out.println();

        //Пример №3. Для объединения всех элементов в один список, включая индекс элемента
        JEnumerable.from(peoples)
                .selectMany((person, index) -> new List<>("Имя чела №" + ++index + ": " + person.getName() + ", Возраст " + person.getAge()))
                .forEach(System.out::println);

        System.out.println();

        //Пример №4. Для преобразования элементов в один список, затем объединение этого списка с другим списком
        JEnumerable.from(peoples)
                .selectMany(
                        Person::getFavoriteLanguages,
                        (people, favLang) -> "Любимый язык программирования " + people.getName() + " - " + favLang
                )
                .forEach(System.out::println);

        //Пример №5. Комбинация примеров № 3, 4
        JEnumerable.from(peoples)
                .selectMany(
                        (person, index) -> new List<>("\nИмя чела №" + ++index + ": " + person.getName() + ", Возраст " + person.getAge() + "\n"),
                        (person, indexedFavLang) -> {
                            StringBuilder values = new StringBuilder(indexedFavLang);

                            for (var item : person.getFavoriteLanguages()) {
                                values.append("Любимый язык программирования ").append(item).append("\n");
                            }

                            return values.toString();
                        }
                )
                .forEach(System.out::print);
    }
}

//Класс для экспериментов :)
class Person {
    private final String _name;
    private final int _age;
    private final List<String> _favoriteLanguages;

    Person(String name, int age, String... favoriteLanguages) {
        _name = name;
        _age = age;
        _favoriteLanguages = new List<>(favoriteLanguages);
    }

    public String getName() {
        return _name;
    }

    public int getAge() {
        return _age;
    }

    public List<String> getFavoriteLanguages() {
        return _favoriteLanguages;
    }
}