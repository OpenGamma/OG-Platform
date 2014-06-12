/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.helpers.MessageFormatter;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;

/**
 * The immutable result from a calculation containing an indication
 * of whether a value has been calculated.
 * Results can be generated using the factory methods on this class.
 *
 * @param <T> the type of the underlying result for a successful invocation
 */
public abstract class Result<T> {

  /**
   * Indicates if this result represents a successful call and has a result available.
   *
   * @return true if the result represents a success and a value is available
   */
  public abstract boolean isSuccess();

  /**
   * Return the actual result value if calculated successfully.
   * <p>
   * If it has not been calculated then an IllegalStateException will be thrown.
   * To avoid this, check the result status using {@link #isSuccess()} or
   * {@link #getStatus()} first.
   *
   * @return the value if calculated successfully, not null
   * @throws IllegalArgumentException if called when the result has not been successfully calculated
   */
  public abstract T getValue();

  /**
   * Indicates the status of this result.
   * <p>
   * It is up to the client to decide if it is able to handle the status or
   * decline to handle. In general it is easier to call {@link #isSuccess()}.
   *
   * @return the status of this function result
   */
  public abstract ResultStatus getStatus();

  /**
   * Return the message associated with a failure event.
   * <p>
   * If the calculation was actually successful then an an IllegalStateException will be thrown.
   * To avoid this, check the result status using {@link #isSuccess()}
   * or {@link #getStatus()} first.
   *
   * @return the failure message if calculation was unsuccessful, not null
   * @throws IllegalStateException if called on a success result
   */
  public abstract String getFailureMessage();

  /**
   * Gets the collection of failure instances that are associated with this result.
   * <p>
   * If the calculation was actually successful then an an IllegalStateException will be thrown.
   * To avoid this, check the result status using {@link #isSuccess()}
   * or {@link #getStatus()} first.
   * 
   * @return the failures associated with a failure result, empty if successful
   */
  public abstract ImmutableSet<Failure> getFailures();

  /**
   * Applies a function to a result's value if the result is a success.
   * If the result is a failure then a failure is returned without applying the function.
   * Useful for applying logic to a successful result using Java 8 lambdas without having to check the status.
   * <pre>
   *   result = ...
   *   return result.ifSuccess(value -> doSomething(value));
   * </pre>
   * Identical to {@link #flatMap}
   *
   * @param <U>  the required type of the new result object
   * @param function  the function to transform the value with, not null
   * @return the new result, not null
   */
  public abstract <U> Result<U> ifSuccess(Function<T, Result<U>> function);

  /**
   * Applies a function to a result's value if the result is a success.
   * If the result is a failure then a failure is returned without applying the function.
   * Useful for applying logic to a successful result using Java 8 lambdas without having to check the status.
   * <pre>
   *   result = ...
   *   return result.flatMap(value -> doSomething(value));
   * </pre>
   * Identical to {@link #ifSuccess}
   *
   * @param <U>  the required type of the new result object
   * @param function  the function to transform the value with, not null
   * @return the new result, not null
   */
  public <U> Result<U> flatMap(Function<T, Result<U>> function) {
    return ifSuccess(function);
  }

  /**
   * Combines this result's value with another result's value using a binary function if both are successes.
   * If either result is a failure then a failure is returned without applying the function.
   * Useful for applying logic to successful results using Java 8 lambdas without having to check the statuses.
   * <pre>
   *   result1 = ...
   *   result2 = ...
   *   return result1.combineWith(result2, (value1, value2) -> doSomething(value1, value2);
   * </pre>
   *
   * @param other  another result
   * @param function  a function for combining values from two results
   * @param <U> the type of the other result's value
   * @param <V> the type of the value in the returned result
   * @return a the result of combining the result values or a failure if either result is a failure
   */
  public abstract <U, V> Result<V> combineWith(Result<U> other, Function2<T, U, Result<V>> function);

  //-------------------------------------------------------------------------
  /**
   * Indicates if there is a result value available from this instance.
   * <p>
   * This generally means that any calculation has been successfully performed
   * but for calculation that may return partial results e.g. market data
   * requests this method will return true. To distinguish between these
   * cases, check the result status using {@link #getStatus()}.
   *
   * @return true if a value is available
   * @deprecated use {@link #isSuccess()}
   */
  @Deprecated
  public boolean isValueAvailable() {
    return isSuccess();
  }

  /**
   * Propagate a failure result, ensuring that its generic type signature
   * matches the one required.
   *
   * @param <U>  the required type of the new result object
   * @return the new function result object, not null
   * @throws IllegalStateException if invoked on a successful result
   * @deprecated use {@link #failure(Result)}
   */
  @Deprecated
  public <U> Result<U> propagateFailure() {
    return Result.failure(this);
  }

  /**
   * Applies a function to a result's value if the result is a success.
   * If the result is a failure then a failure is returned without applying the function.
   *
   * @param <U>  the required type of the new result object
   * @param function  the mapper object to transform the value with, not null
   * @return the new result, not null
   * @deprecated use {@link #ifSuccess(Function)} or {@link #flatMap(Function)}
   */
  @Deprecated
  public <U> Result<U> map(final ResultMapper<T, U> function) {
    return flatMap(new Function<T, Result<U>>() {
      @Override
      public Result<U> apply(T input) {
        return function.map(getValue());
      }
    });
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a successful result wrapping a value
   *
   * @param value  the result value
   * @param <U> the type of the value
   * @return a successful result wrapping the value
   */
  public static <U> Result<U> success(U value) {
    return new SuccessResult<>(value);
  }

  /**
   * Creates a failed result.
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   *
   * @param status  the result status
   * @param message  a message explaining the failure, uses the SLF4J message format for inserting {@code messageArgs}
   * @param messageArgs  the arguments for the message
   * @param <U> the expected type of the result
   * @return a failure result
   */
  public static <U> Result<U> failure(FailureStatus status, String message, Object... messageArgs) {
    return FailureResult.of(new Failure(status, formatMessage(message, messageArgs)));
  }

  /**
   * Creates a failed result caused by an exception.
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   *
   * @param exception  the cause of the failure
   * @param message  a message explaining the failure, uses the SLF4J message format for inserting {@code messageArgs}
   * @param messageArgs  the arguments for the message
   * @param <U> the expected type of the result
   * @return a failure result
   */
  public static <U> Result<U> failure(Exception exception, String message, Object... messageArgs) {
    return FailureResult.of(new Failure(exception, formatMessage(message, messageArgs)));
  }

  /**
   * Creates a failed result caused by an exception.
   *
   * @param exception  the cause of the failure
   * @param <U> the expected type of the result
   * @return a failure result
   */
  public static <U> Result<U> failure(Exception exception) {
    return FailureResult.of(new Failure(exception));
  }

  /**
   * Creates a failed result caused by an exception with a specified status.
   *
   * @param status  the result status
   * @param exception  the cause of the failure
   * @param <U> the expected type of the result
   * @return a failure result
   */
  public static <U> Result<U> failure(FailureStatus status, Exception exception) {
    return FailureResult.of(new Failure(status, exception));
  }

  /**
   * Creates a failed result caused by an exception with a specified status and message.
   * <p>
   * Formatting of the error message uses placeholders as per SLF4J.
   * Each {} in the message is replaced by the next message argument.
   *
   * @param status  the result status
   * @param exception  the cause of the failure
   * @param message  a message explaining the failure, uses the SLF4J message format for inserting {@code messageArgs}
   * @param messageArgs  the arguments for the message
   * @param <U> the expected type of the result
   * @return a failure result
   */
  public static <U> Result<U> failure(FailureStatus status, Exception exception, String message, Object... messageArgs) {
    return FailureResult.of(new Failure(status, formatMessage(message, messageArgs), exception));
  }

  /**
   * Formats the message using SLF4J.
   * 
   * @param message  the message
   * @param messageArgs  the arguments for the message
   * @return the formatted message
   */
  private static String formatMessage(String message, Object[] messageArgs) {
    return MessageFormatter.arrayFormat(message, messageArgs).getMessage();
  }

  /**
   * Returns a failed result from another failed result.
   * This method ensures the result type matches the expected type.
   *
   * @param result  a failure result
   * @param <U> the expected result type
   * @return a failure result of the expected type
   * @throws IllegalArgumentException if the result is a success
   */
  @SuppressWarnings("unchecked")
  public static <U> Result<U> failure(Result<?> result) {
    if (result.isSuccess()) {
      throw new IllegalArgumentException("Result must be a failure");
    }
    return (Result<U>) result;
  }

  /**
   * Creates a failed result cause by multiple failed results.
   * The input results can be successes or failures, only the failures will be included in the created result.
   * Intended to be used with {@link #anyFailures(Result[])}.
   * <code>
   *   if (Result.anyFailures(result1, result2, result3) {
   *     return Result.failures(result1, result2, result3);
   *   }
   * </code>
   *
   * @param result1  the first result
   * @param result2  the second result
   * @param results  the rest of the results
   * @param <U> the expected type of the result
   * @return a failed result wrapping multiple other failed results
   * @throws IllegalArgumentException if all of the results are successes
   */
  public static <U> Result<U> failure(Result<?> result1, Result<?> result2, Result<?>... results) {
    ArgumentChecker.notNull(result1, "result1");
    ArgumentChecker.notNull(result2, "result2");

    List<Failure> failures = new ArrayList<>();
    if (!result1.isSuccess()) {
      failures.addAll(result1.getFailures());
    }
    if (!result2.isSuccess()) {
      failures.addAll(result2.getFailures());
    }
    for (Result<?> result : results) {
      if (!result.isSuccess()) {
        failures.addAll(result.getFailures());
      }
    }
    if (failures.isEmpty()) {
      throw new IllegalArgumentException("All results were successes");
    } else {
      return FailureResult.of(failures);
    }
  }

  /**
   * Creates a failed result cause by multiple failed results.
   * The input results can be successes or failures, only the failures will be included in the created result.
   * Intended to be used with {@link #anyFailures(Iterable)}.
   * <code>
   *   if (Result.anyFailures(results) {
   *     return Result.failure(results);
   *   }
   * </code>
   *
   * @param results  multiple results, of which at least one must be a failure, not empty
   * @param <U> the expected type of the result
   * @return a failed result wrapping multiple other failed results
   * @throws IllegalArgumentException if results is empty or contains nothing but successes
   */
  public static <U> Result<U> failure(Iterable<Result<?>> results) {
    ArgumentChecker.notEmpty(results, "results");

    List<Failure> failures = new ArrayList<>();
    for (Result<?> result : results) {
      if (!result.isSuccess()) {
        failures.addAll(result.getFailures());
      }
    }
    if (failures.isEmpty()) {
      throw new IllegalArgumentException("All results were successes");
    } else {
      return FailureResult.of(failures);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if all the results are successful.
   * 
   * @param results  the results to check
   * @return true if all of the results are successes
   */
  public static boolean allSuccessful(Result<?>... results) {
    for (Result<?> result : results) {
      if (!result.isSuccess()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if all the results are successful.
   * 
   * @param results  the results to check
   * @return true if all of the results are successes
   */
  public static boolean allSuccessful(Iterable<? extends Result<?>> results) {
    for (Result<?> result : results) {
      if (!result.isSuccess()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if any of the results are failures.
   * 
   * @param results  the results to check
   * @return true if any of the results are failures
   */
  public static boolean anyFailures(Result<?>... results) {
    return !allSuccessful(results);
  }

  /**
   * Checks if any of the results are failures.
   * 
   * @param results  the results to check
   * @return true if any of the results are failures
   */
  public static boolean anyFailures(Iterable<? extends Result<?>> results) {
    return !allSuccessful(results);
  }

}
