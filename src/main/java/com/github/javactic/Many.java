package com.github.javactic;
/*
 *    ___                       _   _
 *   |_  |                     | | (_)
 *     | | __ ___   ____ _  ___| |_ _  ___
 *     | |/ _` \ \ / / _` |/ __| __| |/ __|
 * /\__/ / (_| |\ V / (_| | (__| |_| | (__   -2015-
 * \____/ \__,_| \_/ \__,_|\___|\__|_|\___|
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.io.Serializable;
import java.util.Objects;

/**
 * An {@link Every} that contains two or more elements. 
 *
 * @author mvh
 *
 * @param <T> the type of the many
 */
public final class Many<T> implements Every<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private final Vector<T> elements;

  @SafeVarargs
  public static <T> Many<T> of(T first, T second, T... rest) {
    Objects.requireNonNull(first, "first of Many cannot be null");
    Objects.requireNonNull(first, "second of Many cannot be null");
    Objects.requireNonNull(rest, "rest of Many cannot be null");
    return new Many<>(first, Vector.of(rest).prepend(second));
  }

  Many(T first, Seq<? extends T> rest) {
    elements = Vector.of(first).appendAll(rest);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Many && elements.eq(((Many<T>) obj).elements);
  }

  public int hashCode() {
    return toVector().hashCode();
  }

  @Override
  public String toString() {
    return String.format("Many(%s)", toVector().mkString(", "));
  }

  @Override
  public Vector<T> toVector() {
    return elements;
  }

}
