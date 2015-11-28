/**
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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.github.javactic;

import java.io.Serializable;

/**
 * Indicates a validation failed, describing the failure with a contained error value.
 * 
 * @author mvh
 *
 * @param <T> the type of value describing a validation failure for this Fail
 */
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