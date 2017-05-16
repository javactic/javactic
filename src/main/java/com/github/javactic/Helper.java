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

import io.vavr.collection.IndexedSeq;

import java.util.regex.Pattern;

class Helper {

  private static final String REPLACE = Pattern.quote("{}");

  static <E> Every<E> fromNonEmptySeq(IndexedSeq<? extends E> seq) {
    return Every.of(seq.head(), seq.tail());
  }

  static String parse(String msg, Object... args) {
    if (msg == null) return null;
    for (Object arg : args) {
      msg = msg.replaceFirst(REPLACE, arg.toString());
    }
    return msg;
  }

}
