package com.github.javactic.doc;

import com.github.javactic.Accumulation;
import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Pass;
import com.github.javactic.Validation;

import javaslang.Tuple2;
import javaslang.collection.List;

public class AccumulatingOrExample {

    Or<String, Every<String>> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Good.of(trimmed) : Bad.ofOneString("'{}' is not a valid name", input);
    }

    Or<Integer, Every<String>> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Good.of(age) : Bad.ofOneString("'{}' is not a valid age", age);
        } catch (NumberFormatException e) {
            return Bad.ofOneString("'{}' is not a valid integer", input);
        }
    }

    Or<Person, Every<String>> parsePerson(String inputName, String inputAge) {
        Or<String, Every<String>> name = parseName(inputName);
        Or<Integer, Every<String>> age = parseAge(inputAge);
        return Accumulation.withGood(name, age, (n, a) -> new Person(n, a));
    }

    void print() {
        Or<Person, Every<String>> or = parsePerson("Bridget Jones", "29");
        System.out.println(or);
        // Result: Good(Person(Bridget Jones,29))

        or = parsePerson("Bridget Jones", "");
        System.out.println(or);
        // Result: Bad(One("" is not a valid integer))

        or = parsePerson("Bridget Jones", "-29");
        System.out.println(or);
        // Result: Bad(One("-29" is not a valid age))

        or = parsePerson("", "");
        System.out.println(or);
        // Result: Bad(Many("" is not a valid name, "" is not a valid integer))
    }
    
    void combined() {
        List<Or<Integer, Every<String>>> list = List.of(parseAge("29"), parseAge("30"), parseAge("31"));
        Accumulation.combined(list, List.collector());  // Result: Good(List(29, 30, 31))

        List<Or<Integer, Every<String>>> list2 = List.of(parseAge("29"), parseAge("-30"), parseAge("31"));
        Accumulation.combined(list2, List.collector()); // Result: Bad(One("-30" is not a valid age))

        List<Or<Integer, Every<String>>> list3 = List.of(parseAge("29"), parseAge("-30"), parseAge("-31"));
        Accumulation.combined(list3, List.collector()); // Result: Bad(Many("-30" is not a valid age, "-31" is not a valid age))
    }
    
    void validatedBy() {
        List<String> list = List.of("29", "30", "31");
        Accumulation.validatedBy(list, this::parseAge, List.collector());  // Result: Good(List(29, 30, 31))

        List<String> list2 = List.of("29", "-30", "31");
        Accumulation.validatedBy(list2, this::parseAge, List.collector()); // Result: Bad(One("-30" is not a valid age))

        List<String> list3 = List.of("29", "-30", "-31");
        Accumulation.validatedBy(list3, this::parseAge, List.collector()); 
            // Result: Bad(Many("-30" is not a valid age, "-31" is not a valid age))
    }
    
    void zip() {
      Or<Tuple2<String, Integer>, Every<String>> zip = Accumulation.zip(parseName("Dude"), parseAge("21"));
      // Result: Good((Dude,21))

      Accumulation.zip(parseName("Dude"), parseAge("-21"));
      // Result: Bad(One("-21" is not a valid age))

      Accumulation.zip(parseName(""), parseAge("-21"));
      // Result: Bad(Many("" is not a valid name, "-21" is not a valid age))
    }
    
    Validation<String> isRound(int i) {
        return (i % 10 == 0) ? Pass.instance() : Fail.of(i + " was not a round number");
    }

    Validation<String> isDivBy3(int i) {
        return (i % 3 == 0) ? Pass.instance() : Fail.of(i + " was not divisible by 3");
    }
    
    void when() {
      Or<Integer, Every<String>> when = Accumulation.when(parseAge("-30"), this::isRound, this::isDivBy3);
      //Result: Bad(One("-30" is not a valid age))
        
      Accumulation.when(parseAge("30"), this::isRound, this::isDivBy3);
      //Result: Good(30)
        
      Accumulation.when(parseAge("33"), this::isRound, this::isDivBy3);
      //Result: Bad(One(33 was not a round number))

      Accumulation.when(parseAge("20"), this::isRound, this::isDivBy3);
      //Result: Bad(One(20 was not divisible by 3))

      Accumulation.when(parseAge("31"), this::isRound, this::isDivBy3);
      //Result: Bad(Many(31 was not a round number, 31 was not divisible by 3))    
    }
    
    public static void main(String[] args) {
        new AccumulatingOrExample().print();
    }

}
