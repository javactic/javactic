package com.github.javactic;

import javaslang.collection.List;
import javaslang.control.Option;

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
List<String> list = List.ofAll("29", "30", "31");
Accumulation.validatedBy(list, Doc::parseAge, List.collector());
    // Result: Good(List(29, 30, 31))

 List<String> list2 = List.ofAll("29", "-30", "31");
 Accumulation.validatedBy(list2, Doc::parseAge, List.collector());
 // Result: Bad(One("-30" is not a valid age))

 List<String> list3 = List.ofAll("29", "-30", "-31");
 Accumulation.validatedBy(list3, Doc::parseAge, List.collector());
 // Result: Bad(Many("-30" is not a valid age, "-31" is not a valid age))
    }

}
