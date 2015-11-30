package com.github.javactic;

import javaslang.Tuple2;
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
    
    /*
def isRound(i: Int): Validation[ErrorMessage] =
    if (i % 10 == 0) Pass else Fail(i + " was not a round number")

def isDivBy3(i: Int): Validation[ErrorMessage] =
  if (i % 3 == 0) Pass else Fail(i + " was not divisible by 3")
     * */
    
static Validation<String> isRound(int i) {
    return (i % 10 == 0) ? Pass.instance() : Fail.of(i + " was not a round number");
}

static Validation<String> isDivBy3(int i) {
    return (i % 3 == 0) ? Pass.instance() : Fail.of(i + " was not divisible by 3");
}
    
    public static void main(String[] args) {
Or<Tuple2<String, Integer>, Every<String>> zip = Accumulation.zip(parseName("Dude"), parseAge("21"));
// Result: Good((Dude,21))

Accumulation.zip(parseName("Dude"), parseAge("-21"));
// Result: Bad(One("-21" is not a valid age))

Accumulation.zip(parseName(""), parseAge("-21"));
// Result: Bad(Many("" is not a valid name, "-21" is not a valid age))

Or<Integer, Every<String>> when = Accumulation.when(parseAge("-30"), Doc::isRound, Doc::isDivBy3);
//Result: Bad(One("-30" is not a valid age))

Accumulation.when(parseAge("30"), Doc::isRound, Doc::isDivBy3);
//Result: Good(30)

Accumulation.when(parseAge("33"), Doc::isRound, Doc::isDivBy3);
//Result: Bad(One(33 was not a round number))

Accumulation.when(parseAge("20"), Doc::isRound, Doc::isDivBy3);
//Result: Bad(One(20 was not divisible by 3))

Accumulation.when(parseAge("31"), Doc::isRound, Doc::isDivBy3);
//Result: Bad(Many(31 was not a round number, 31 was not divisible by 3))    
    }

}
