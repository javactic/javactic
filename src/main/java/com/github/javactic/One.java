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

import io.vavr.collection.Vector;

import java.io.Serializable;
import java.util.Objects;

/**
 * An {@link Every} that contains exactly one element.
 *
 * @author mvh
 *
 * @param <T> the type of the element.
 */
public final class One<T> implements Every<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private final Vector<T> elements;

  /**
   * Creates a One.
   *
   * @param <T> the type of the Or
   * @param loneElement the parameter of the One
   * @return a One
   */
  public static <T> One<T> of(T loneElement) {
    return new One<>(loneElement);
  }

  /**
   * Creates a One of String based on the given message. The message can be a
   * slf4j string with {} placeholders that will be replaced by the given
   * optional arguments.
   *
   * @param msg
   *            the message string with possible placeholders
   * @param args
   *            the values to replace the placeholders with
   * @return a One
   */
  public static One<String> ofString(String msg, Object... args) {
    return One.of(Helper.parse(msg, args));
  }

  private One(T onlyElement) {
    Objects.requireNonNull(onlyElement, "only element of One cannot be null");
    elements = Vector.of(onlyElement);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    One<?> one = (One<?>) o;
    return elements.equals(one.elements);
  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }

  @Override
  public String toString() {
    return String.format("One(%s)", toVector().mkString());
  }

  @Override
  public Vector<T> toVector() {
    return elements;
  }
}
