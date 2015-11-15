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

import java.io.Serializable;

public class Fail<T> implements Validation<T>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	final T error;
	
	public static <T> Fail<T> of(T error) {
		return new Fail<>(error);
	}
	
	Fail(T error) {
		this.error = error;
	}
	
	@Override
	public Validation<T> and(Validation<T> other) {
		return this;
	}

	@Override
	public boolean isPass() {
		return false;
	}

	@Override
	public boolean isFail() {
		return true;
	}

	@Override
	public T getError() {
		return error;
	}
	
}