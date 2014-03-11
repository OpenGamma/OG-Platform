/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * The immutable result from a calculation containing an indication
 * of whether a value has been calculated. FunctionResults can be
 * generated using the methods on {@link ResultGenerator}.
 *
 * @param <T> the type of the underlying result for a successful invocation
 */
public interface Result<T> {

  /**
   * Indicates if there is a result value available from this instance.
   * <p>
   * This generally means that any calculation has been successfully performed
   * but for calculation that may return partial results e.g. market data
   * requests this method will return true. To distinguish between these
   * cases, check the result status using {@link #getStatus()}.
   *
   * @return true if a value is available
   * TODO too long winded. replace with isSuccess()? isAvailable()?
   */
  boolean isValueAvailable();

  /**
   * Return the actual result value if calculated successfully.
   * <p>
   * If it has not been calculated then an IllegalStateException will be thrown.
   * To avoid this, check the result status using {@link #isValueAvailable()} or
   * {@link #getStatus()} first.
   *
   * @return the value if calculated successfully, not null
   * @throws IllegalArgumentException if called when the result has not been successfully calculated
   */
  T getValue();

  /**
   * Indicates the status of this result.
   * <p>
   * It is up to the client to decide if it is able to handle the status or
   * decline to handle. In general it is easier to call {@link #isValueAvailable()}.
   *
   * @return the status of this function result
   */
  ResultStatus getStatus();

  /**
   * Return the message associated with a failure event.
   * <p>
   * If the calculation was actually successful then an an IllegalStateException will be thrown.
   * To avoid this, check the result status using {@link #isValueAvailable()}
   * or {@link #getStatus()} first.
   *
   * @return the failure message if calculation was unsuccessful, not null
   * @throws IllegalArgumentException if called when the result value has been successfully calculated
   */
  String getFailureMessage();

  /**
   * Propagate a failure result, ensuring that its generic type signature
   * matches the one required.
   *
   * @param <U>  the required type of the new result object
   * @return the new function result object, not null
   * @throws IllegalStateException if invoked on a successful result
   */
  <U> Result<U> propagateFailure();

  /**
   * Transforms this result into one containing a type U.
   * If the passed result is a failure then the original object will be passed through unaltered.
   *
   * @param <U>  the required type of the new result object
   * @param mapper  the mapper object to transform the value with, not null
   * @return the new result, not null
   * TODO should this be called onSuccess?
   */
  <U> Result<U> map(ResultMapper<T, U> mapper);
}
