package com.github.javactic;

public class Doc {

    static class Person {
        private final String name;
        private final int age;
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        @Override
        public String toString() {
            return "Person(" + name + "," + age + ")";
        }
    }
/*
    static Option<String> parseName(String name) {
        String trimmed = name.trim();
        return (trimmed.isEmpty()) ? Option.none() : Option.of(trimmed);
    }
    
    static Option<Integer> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Option.of(age) : Option.none(); 
        } catch (NumberFormatException e) {
            return Option.none();
        }
    }
    
    static Option<Person> parsePerson(String inputName, String inputAge) {
        return parseName(inputName).flatMap(name -> 
            parseAge(inputAge).map(age -> new Person(name, age))
        );
    }
    */
    /*
parsePerson("Bridget Jones", "29")
// Result: Some(Person(Bridget Jones,29))

parsePerson("Bridget Jones", "")
// Result: None

parsePerson("Bridget Jones", "-29")
// Result: None

parsePerson("", "")
// Result: None

     */
    
    /*
     def parseName(input: String): Either[String, String] = {
  val trimmed = input.trim
  if (!trimmed.isEmpty) Right(trimmed) else Left(s""""${input}" is not a valid name""")
}
     */
    /*
    static Either<String, String> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Either.right(trimmed) : Either.left(input + "is not a valid name");
    }
    
    static Either<String, Integer> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Either.right(age) : Either.left(age + "is not a valid age"); 
        } catch (NumberFormatException e) {
            return Either.left(input + "is not a valid integer");
        }
    }
    
    static Either<String, Person> parsePerson(String inputName, String inputAge) {
        return parseName(inputName).right()
                .flatMap(name -> parseAge(inputAge).right().map(age -> new Person(name, age)))
                .toEither();
    }
    */
    
    /*
    static Or<String, String> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Or.good(trimmed) : Or.bad("\"" + input + "\" is not a valid name");
    }
    
    static Or<Integer, String> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Or.good(age) : Or.bad("\"" + age + "\" is not a valid age"); 
        } catch (NumberFormatException e) {
            return Or.bad("\"" + input + "\" is not a valid integer");
        }
    }
    
    static Or<Person, String> parsePerson(String inputName, String inputAge) {
        return parseName(inputName)
                .flatMap(name -> parseAge(inputAge).map(age -> new Person(name, age)));
    }
    */
    
    static Or<String, One<String>> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Good.of(trimmed) : Bad.ofOne("'" + input + "' is not a valid name");
    }
    
    static Or<Integer, One<String>> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Good.of(age) : Bad.ofOne("'" + age + "' is not a valid age"); 
        } catch (NumberFormatException e) {
            return Bad.ofOne("'" + input + "' is not a valid integer");
        }
    }
    
    static Or<Person, Every<String>> parsePerson(String inputName, String inputAge) {
        Or<String, One<String>> name = parseName(inputName);
        Or<Integer, One<String>> age = parseAge(inputAge);
        return Accumulation.withGood(name, age, (n, a) -> new Person(n, a));
    }

    static Or<String, One<String>> parseString(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Good.of(trimmed) : Bad.ofOne("'" + input + "' is not a valid name");
    }
    
    public static void main(String[] args) {

One.of(1);
Many.of(1, 3);
Many.of(1, 2, 3);

Every.of(1);
Every.of(1, 2);
Every.of(1, 2, 3);

Many.of(1, 2, 3).map(i -> i + 1);                   // Result: Many(2, 3, 4)
One.of(1).map(i -> i + 1);                          // Result: One(2)
Every.of(1, 2, 3).containsSlice(Every.of(2, 3));    // Result: true
Every.of(1, 2, 3).containsSlice(Every.of(3, 4));    // Result: false
Every.of(-1, -2, 3, 4, 5).minBy(i -> Math.abs(i));  // Result: -1

Every.of(1, 2, 3).toSeq().filter(i -> i < 10); // Result: Vector(1, 2, 3)
Every.of(1, 2, 3).toSeq().filter(i -> i > 10); // Result: Vector()
        
    }

}
