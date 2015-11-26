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
package com.github.javactic;

import javaslang.collection.IndexedSeq;

class Helper {

	static <E> Every<E> fromNonEmptySeq(IndexedSeq<E> seq) {
		return Every.of(seq.head(), seq.tail());
	}
	
	static <X> X niy() {
		throw new UnsupportedOperationException("until basetype implements required functionality");
	}
}
