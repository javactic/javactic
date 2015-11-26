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

import java.io.Serializable;
import java.util.NoSuchElementException;

public class Pass<T> implements Validation<T>, Serializable {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public static <T> Pass<T> instance() {
		return (Pass<T>) PASS;
	}
	
	private Pass() {}
	
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

}