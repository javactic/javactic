package com.github.javactic.doc;

class Person {
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