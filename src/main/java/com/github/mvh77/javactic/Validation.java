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


/**
 * Represents the result of a <em>validation</em>, either the object {@link Pass} if the validation 
 * succeeded, else an instance of {@link Fail} containing an error value describing the validation failure.
 *
 * <p>
 * <code>Validation</code>s are used to filter {@link Or}s in <code>filter</code> method calls.
 * For example, consider these methods:
 * </p>
 *
 * <pre>{@code
 * Validation&lt;String&gt; isRound(int i) {
 *   return (i % 10 == 0) ? Pass.instance() : Fail.of(i + " was not a round number");
 * }
 * 
 * Validation&lt;String&gt; isDivBy3(int i) {
 *   return (i % 3 == 0) ? Pass.instance() : Fail.of(i + " was not divisible by 3");
 * }}</pre>
 *
 * <p>
 * Because <code>isRound</code> and <code>isDivBy3</code> take an <code>Int</code> and return a <code>Validation&lt;String&gt;</code>, you
 * can use them in filters involving <code>Or</code>s of type <code>Or&lt;Int,String&gt;</code>.
 * Here's an example:
 * </p>
 *
 * <pre>{@code
 * Or.&lt;Integer, String&gt;good(123).filter(this::isRound).filter(this::isDivBy3);
 * // Result: Bad(123 was not a round number)
 * }}</pre>
 *
 * <p>
 * {@link Validation}s can also be used to accumulate error using <code>when</code>, a method that's made available by class {@link Accumulation}
 * on accumulating <code>Or</code>s (<code>Or</code>s whose <code>Bad</code> type is an <code>Every&lt;T&gt;</code>). Here are some examples:
 * </p>
 *
 * <pre>{@code
 * Accumulation.when(Or.good(3), this::isRound, this::isDivBy3);
 * // Result: Bad(One(3 was not a round number))
 * 
 * Accumulation.when(Or.good(4), this::isRound, this::isDivBy3);
 * // Result: Bad(Many(4 was not a round number, 4 was not divisible by 3))
 * }</pre>
 *
 * <p>
 * Note: You can think of <code>Validation</code> as an &ldquo;<code>Option</code> with attitude,&rdquo; where <code>Pass</code> is 
 * a <code>None</code> that indicates validation success and <code>Fail</code> is a <code>Some</code> whose value describes 
 * the validation failure.
 * </p>
 * 
 * @param <E> the type of error value describing a validation failure for this <code>Validation</code>
 */
public interface Validation<E> {

	public static <E> Validation<E> pass() {
		return Pass.instance();
	}
	
	public static <E> Validation<E> fail(E reason) {
		return Fail.of(reason);
	}
	
	public abstract Validation<E> and(Validation<E> other);
	
	abstract boolean isPass();
	abstract boolean isFail();
	abstract E getError();
}
