package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Or;

import java.util.function.Function;
import java.util.function.Supplier;

public class FutureFactory<B> {

  private final Function<? super Exception, ? extends B> converter;

  public FutureFactory(Function<? super Exception, ? extends B> exceptionConverter) {
    this.converter = exceptionConverter;
  }

  @SuppressWarnings("unchecked")
  public <G> OrFuture<G, B> future(Supplier<? extends Or<G, ? extends B>> supp) {
    return OrFuture.of(() -> {
      try {
        return supp.get();
      } catch (Exception e) {
        return Bad.of(converter.apply(e));
      }
    });
  }

  public static final FutureFactory<String> OF_EXCEPTION_MESSAGE = new FutureFactory<>(Throwable::getMessage);
}
