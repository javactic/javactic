/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mvh77.javactic;

import javaslang.collection.Seq;
import javaslang.collection.Vector;

public class Many<T> implements Every<T> {

	private final Vector<T> elements;
	
	@SafeVarargs
	public static <T> Many<T> of(T first, T second, T... rest) {
		return new Many<>(first, Vector.of(rest).prepend(second));
	}
	
	Many(T first, Seq<? extends T> rest) {
		elements = Vector.of(first).appendAll(rest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Many) {
			return elements.eq(((Many<T>)obj).elements);
		}
		return false;
	}
	
	public int hashCode() {
		return toVector().hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("Many(%s)", toVector());
	}

	@Override
	public Vector<T> toVector() {
		return elements;
	}


}
