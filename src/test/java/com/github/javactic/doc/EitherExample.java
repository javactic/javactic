package com.github.javactic.doc;

import javaslang.control.Either;

public class EitherExample {

    Either<String, String> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Either.right(trimmed) : Either.left("'" + input + "' is not a valid name");
    }
    
    Either<String, Integer> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Either.right(age) : Either.left("'" + age + "' is not a valid age");
        } catch (NumberFormatException e) {
            return Either.left("'" + input + "' is not a valid integer");
        }
    }
    
    Either<String, Person> parsePersonEither(String inputName, String inputAge) {
        return parseName(inputName)
                .flatMap(name -> parseAge(inputAge)
                        .map(age -> new Person(name, age)));
    }
    
    void print() {
        Either<String, Person> either = parsePersonEither("Bridget Jones", "29");
        System.out.println(either);
        //Result: Right(Person(Bridget Jones,29))

        either = parsePersonEither("Bridget Jones", "");
        System.out.println(either);
        //Result: Left("" is not a valid integer)

        either = parsePersonEither("Bridget Jones", "-29");
        System.out.println(either);
        //Result: Left("-29" is not a valid age)

        either = parsePersonEither("", "");
        System.out.println(either);
        //Result: Left("" is not a valid name)
    }

    public static void main(String[] args) {
        new EitherExample().print();
    }
    
}
