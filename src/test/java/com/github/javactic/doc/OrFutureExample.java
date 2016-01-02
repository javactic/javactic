package com.github.javactic.doc;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.futures.OrFuture;

public class OrFutureExample {
    
    Or<String, String> parseName(String input) {
        String trimmed = input.trim();
        return (!trimmed.isEmpty()) ? Good.of(trimmed) : Bad.ofString("'{}' is not a valid name", input);
    }

    Or<Integer, String> parseAge(String input) {
        try {
            int age = Integer.parseInt(input.trim());
            return (age >= 0) ? Good.of(age) : Bad.ofString("'{}' is not a valid age", age);
        } catch (NumberFormatException e) {
            return Bad.ofString("'{}' is not a valid integer", input);
        }
    }

    Or<Person, String> parsePerson(String inputName, String inputAge) {
        return parseName(inputName).flatMap(name -> parseAge(inputAge).map(age -> new Person(name, age)));
    }

    // --- async ---
    
    OrFuture<String, String> parseNameAsync(String input) {
        return OrFuture.of(() -> parseName(input));
    }
    
    OrFuture<Integer, String> parseAgeAsync(String input) {
        return OrFuture.of(() -> parseAge(input));
    }
    
    OrFuture<Person, String> parsePersonAsync(String inputName, String inputAge) {
        return parseNameAsync(inputName)
                .flatMap(name -> parseAgeAsync(inputAge)
                        .map(age -> new Person(name, age)));
    }

    void print() throws Exception {
        OrFuture<Person, String> asyncOr = parsePersonAsync("Bridget Jones", "29");
        asyncOr.onComplete(or -> System.out.println(or));
        // Result: Good(Person(Bridget Jones,29))

        asyncOr = parsePersonAsync("Bridget Jones", "");
        asyncOr.onComplete(or -> System.out.println(or));
        // Result: Bad("" is not a valid integer)

        asyncOr = parsePersonAsync("Bridget Jones", "-29");
        asyncOr.onComplete(or -> System.out.println(or));
        // Result: Bad("-29" is not a valid age)

        asyncOr = parsePersonAsync("", "");
        asyncOr.onComplete(or -> System.out.println(or));
        // Result: Bad("" is not a valid name)
    }

    
    public static void main(String[] args) throws Exception {
        OrFutureExample ex = new OrFutureExample();
        ex.print();
        Thread.sleep(1000);

    }

}
