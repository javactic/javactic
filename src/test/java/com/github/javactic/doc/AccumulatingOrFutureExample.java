//package com.github.javactic.doc;
//
//import com.github.javactic.Bad;
//import com.github.javactic.Every;
//import com.github.javactic.Fail;
//import com.github.javactic.Good;
//import com.github.javactic.One;
//import com.github.javactic.Or;
//import com.github.javactic.Pass;
//import com.github.javactic.Validation;
//import com.github.javactic.futures.FutureFactory;
//import com.github.javactic.futures.OrFuture;
//import javaslang.Tuple2;
//import javaslang.collection.List;
//import javaslang.collection.Vector;
//
//import java.util.function.Function;
//
//public class AccumulatingOrFutureExample {
//
//  Or<String, One<String>> parseName(String input) {
//    String trimmed = input.trim();
//    return (!trimmed.isEmpty()) ? Good.of(trimmed) : Bad.ofOneString("'{}' is not a valid name", input);
//  }
//
//  Or<Integer, One<String>> parseAge(String input) {
//    try {
//      int age = Integer.parseInt(input.trim());
//      return (age >= 0) ? Good.of(age) : Bad.ofOneString("'{}' is not a valid age", age);
//    } catch (NumberFormatException e) {
//      return Bad.ofOneString("'{}' is not a valid integer", input);
//    }
//  }
//
//  // ---
//
//  Function<Throwable, One<String>> converter = throwable -> One.of(throwable.getMessage());
//  FutureFactory<One<String>> ff = FutureFactory.of(converter);
//
//  OrFuture<String, One<String>> parseNameAsync(String input) {
//    return ff.newFuture(() -> parseName(input));
//  }
//
//  OrFuture<Integer, One<String>> parseAgeAsync(String input) {
//    return ff.newFuture(() -> parseAge(input));
//  }
//
//  OrFuture<Person, Every<String>> parsePersonAsync(String inputName, String inputAge) {
//    OrFuture<String, One<String>> name = parseNameAsync(inputName);
//    OrFuture<Integer, One<String>> age = parseAgeAsync(inputAge);
//    return OrFuture.withGood(name, age, Person::new);
//  }
//
//  void print() {
//    OrFuture<Person, Every<String>> orFuture = parsePersonAsync("Bridget Jones", "29");
//    orFuture.onComplete(System.out::println);
//    // Result: Good(Person(Bridget Jones,29))
//
//    orFuture = parsePersonAsync("Bridget Jones", "");
//    orFuture.onComplete(System.out::println);
//    // Result: Bad(One('' is not a valid integer))
//
//    orFuture = parsePersonAsync("Bridget Jones", "-29");
//    orFuture.onComplete(System.out::println);
//    // Result: Bad(One('-29' is not a valid age))
//
//    orFuture = parsePersonAsync("", "");
//    orFuture.onComplete(System.out::println);
//    // Result: Bad(Many('' is not a valid name, '' is not a valid integer))
//  }
//
//  void combined() {
//    List<OrFuture<Integer, One<String>>> list = List.of(parseAgeAsync("29"), parseAgeAsync("30"), parseAgeAsync("31"));
//    OrFuture.combined(list, List.collector());
//    // Result: Good(List(29, 30, 31))
//
//    List<OrFuture<Integer, One<String>>> list2 = List.of(parseAgeAsync("29"), parseAgeAsync("-30"), parseAgeAsync("31"));
//    OrFuture.combined(list2, List.collector());
//    // Result: Bad(One("-30" is not a valid age))
//
//    List<OrFuture<Integer, One<String>>> list3 = List.of(parseAgeAsync("29"), parseAgeAsync("-30"), parseAgeAsync("-31"));
//    OrFuture.combined(list3, List.collector());
//    // Result: Bad(Many("-30" is not a valid age, "-31" is not a valid age))
//  }
//
//  void validatedBy() {
//    List<String> list = List.of("29", "30", "31");
//    OrFuture.validatedBy(list, this::parseAgeAsync, List.collector());  // Result: Good(List(29, 30, 31))
//
//    List<String> list2 = List.of("29", "-30", "31");
//    OrFuture.validatedBy(list2, this::parseAgeAsync, List.collector()); // Result: Bad(One("-30" is not a valid age))
//
//    List<String> list3 = List.of("29", "-30", "-31");
//    OrFuture.validatedBy(list3, this::parseAgeAsync, List.collector());
//    // Result: Bad(Many("-30" is not a valid age, "-31" is not a valid age))
//  }
//
//  void zip() {
//    OrFuture<Tuple2<String, Integer>, Every<String>> zip = OrFuture.zip(parseNameAsync("Dude"), parseAgeAsync("21"));
//    // Result: Good((Dude,21))
//
//    OrFuture.zip(parseNameAsync("Dude"), parseAgeAsync("-21"));
//    // Result: Bad(One("-21" is not a valid age))
//
//    OrFuture.zip(parseNameAsync(""), parseAgeAsync("-21"));
//    // Result: Bad(Many("" is not a valid name, "-21" is not a valid age))
//  }
//
//  Validation<String> isRound(int i) {
//    return (i % 10 == 0) ? Pass.instance() : Fail.of(i + " was not a round number");
//  }
//
//  Validation<String> isDivBy3(int i) {
//    return (i % 3 == 0) ? Pass.instance() : Fail.of(i + " was not divisible by 3");
//  }
//
//  void when() {
//    OrFuture<Integer, Every<String>> when = OrFuture.when(parseAgeAsync("-30"), Vector.of(this::isRound, this::isDivBy3));
//    //Result: Bad(One("-30" is not a valid age))
//
//    OrFuture.when(parseAgeAsync("30"), Vector.of(this::isRound, this::isDivBy3));
//    //Result: Good(30)
//
//    OrFuture.when(parseAgeAsync("33"), Vector.of(this::isRound, this::isDivBy3));
//    //Result: Bad(One(33 was not a round number))
//
//    OrFuture.when(parseAgeAsync("20"), Vector.of(this::isRound, this::isDivBy3));
//    //Result: Bad(One(20 was not divisible by 3))
//
//    OrFuture.when(parseAgeAsync("31"), Vector.of(this::isRound, this::isDivBy3));
//    //Result: Bad(Many(31 was not a round number, 31 was not divisible by 3))
//
//  }
//
//  public static void main(String[] args) throws InterruptedException {
//    AccumulatingOrFutureExample example = new AccumulatingOrFutureExample();
//    example.print();
//    Thread.sleep(100);
//    example.combined();
//    Thread.sleep(100);
//    example.validatedBy();
//    Thread.sleep(100);
//    example.zip();
//    Thread.sleep(100);
//    example.when();
//    Thread.sleep(100);
//  }
//
//}
