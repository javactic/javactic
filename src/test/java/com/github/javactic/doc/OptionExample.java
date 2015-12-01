package com.github.javactic.doc;

import javaslang.control.Option;

public class OptionExample {

    Option<String> parseName(String name) {
        String trimmed = name.trim();
        return (trimmed.isEmpty()) ? Option.none() : Option.of(trimmed);
    }

    Option<Integer> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Option.of(age) : Option.none();
        } catch (NumberFormatException e) {
            return Option.none();
        }
    }

    Option<Person> parsePerson(String inputName, String inputAge) {
        return parseName(inputName).flatMap(name -> parseAge(inputAge).map(age -> new Person(name, age)));
    }

    void print() {
        Option<Person> option = parsePerson("Bridget Jones", "29");
        System.out.println(option);
        // Result: Some(Person(Bridget Jones,29))

        option = parsePerson("Bridget Jones", "");
        System.out.println(option);
        // Result: None

        option = parsePerson("Bridget Jones", "-29");
        System.out.println(option);
        // Result: None

        option = parsePerson("", "");
        System.out.println(option);
        // Result: None
    }
    
    public static void main(String[] args) {
        new OptionExample().print();
    }

}
