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

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Indicates a validation succeeded.
 *
 * @author mvh
 *
 * @param <T> the type of a failed validation.
 */
public class Pass<T> implements Validation<T>, Serializable {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public static <T> Pass<T> instance() {
    return (Pass<T>) PASS;
  }

  private Pass() {
  }

  private final static Pass<?> PASS = new Pass<>();

  @Override
  public Validation<T> and(Validation<T> other) {
    return other;
  }

  @Override
  public boolean isPass() {
    return true;
  }

  @Override
  public boolean isFail() {
    return false;
  }

  @Override
  public T getError() {
    throw new NoSuchElementException();
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public String toString() {
    return "Pass";
  }

}