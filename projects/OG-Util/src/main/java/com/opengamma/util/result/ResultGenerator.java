/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import java.util.Collection;

import com.google.common.base.Function;

/**
 * Factory class for {@link Result} objects.
 *
 * <p/>
 * <h3>Typical usage pattern:</h3>
 * <pre>
 *
 * import static com.opengamma.util.result.ResultGenerator.failure;
 * import static com.opengamma.util.result.ResultGenerator.propagateFailure;
 * import static com.opengamma.util.result.ResultGenerator.success;
 * import static com.opengamma.util.result.FailureStatus.CALCULATION_FAILED;
 *
 * public class Calculations {
 *
 *   public Result<CalculatedValue> calculate() {
 *
 *     Result<InterimValue> interimResult = doInitialCalculation();
 *     if (interimResult.isValueAvailable()) {
 *       InterimValue value = interimResult.getValue();
 *       Calculator calculator = getCalculator()
 *
 *       if (calculator.isCalculationPossible(value)) {
 *         return calculator.doComplexCalculation(value);
 *       } else {
 *         return failure(CALCULATION_FAILED,
 *             "Cannot calculate as value {} is incompatible with calculator {}", value, calculator);
 *       }
 *     } else {
 *       return propagateFailure(interimResult);
 *     }
 *   }
 * }
 *
 * </pre>
 * @deprecated use the static methods on {@link Result}
 */
@Deprecated
public class ResultGenerator {

  /**
   * Generate a result object indicating that a function completed successfully
   * with the supplied value as the result.
   *
   * @param <T> the type of the value to be returned
   * @param value  the value that the invoked function returned
   * @return a result object wrapping the actual function invocation result, not null
   * @deprecated use {@link Result#success(Object)}
   */
  @Deprecated
  public static <T> Result<T> success(T value) {
    return Result.success(value);
  }

  /**
   * Generate a result object indicating that a function did not complete
   * successfully.
   *
   * @param status  an indication of why the invocation failed, not null
   * @param message  a detailed parameterized message indicating why the function
   *  invocation failed, uses SLF4J placeholders for parameters, not null
   * @param messageArgs  the arguments to be used for the formatted message, not null
   * @param <T> the type of the value which would have been returned if successful
   * @return a result object wrapping the failure details, not null
   * @deprecated use {@link Result#failure(FailureStatus, String, Object...)}
   */
  @Deprecated
  public static <T> Result<T> failure(FailureStatus status, String message, Object... messageArgs) {
    return Result.failure(status, message, messageArgs);
  }

  /**
   * Generate a result object indicating that a function did not complete
   * successfully because of an exception.
   *
   * @param message a description of the problem
   * @param cause the cause of the failure, not null
   * @param <T> the type of the value which would have been returned if successful
   * @return a result object wrapping the failure details, not null
   * @deprecated use {@link Result#failure(Exception, String, Object...)}
   */
  @Deprecated
  public static <T> Result<T> failure(String message, Exception cause) {
    return Result.failure(cause, message);
  }

  /**
   * Generate a result object indicating that a function did not complete
   * successfully because of an exception.
   *
   * @param cause The cause of the failure, not null
   * @param <T> the type of the value which would have been returned if successful
   * @return a result object wrapping the failure details, not null
   * @deprecated use {@link Result#failure(Exception)}
   */
  @Deprecated
  public static <T> Result<T> failure(Exception cause) {
    return Result.failure(cause);
  }

  /**
   * Propagate a failure result, ensuring that its generic type signature
   * matches the one required.
   *
   * @param <T>  the required type of the new result object
   * @param result  the failure to be propagated, not null
   * @return the new function result object, not null
   * @deprecated use {@link Result#failure(Result)}
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static <T> Result<T> propagateFailure(Result<?> result) {
    return Result.failure(result);
  }

  /**
   * Transforms a result containing a type R into one containing a type T.
   * If the passed result is a failure then the original object will be passed through unaltered.
   *
   * @param <T>  the type of the result object to be transformed
   * @param <U>  the required type of the new result object
   * @param result  the result to be transformed, not null
   * @param mapper  the mapper object to transform the value with, not null
   * @return the new function result object, not null
   * @deprecated use {@link Result#ifSuccess(Function)}
   */
  @Deprecated
  public static <T, U> Result<U> map(final Result<T> result, final ResultMapper<T, U> mapper) {
    return result.flatMap(new Function<T, Result<U>>() {
      @Override
      public Result<U> apply(T input) {
        return mapper.map(input);
      }
    });
  }

  /**
   * Check a set of results to see if there are any failures.
   *
   * @param results  the set of results to be checked, not null
   * @return true if the set of results contains at least one failure
   * @deprecated use {@link Result#anyFailures(Result[])}
   */
  @Deprecated
  public static boolean anyFailures(Result<?>... results) {
    return Result.anyFailures(results);
  }

  /**
   * Propagate a set of results as a failure such that multiple failure reasons
   * can be recorded. Any successes included in the set are ignored. If there
   * are no failures in the set then an exception will be thrown.
   *
   * @param <T>  the required type of the new result object
   * @param result1  result to be included in the combined result, not null
   * @param result2  result to be included in the combined result, not null
   * @param results  results to be included in the combined result, not null
   * @return the new function result object
   * @throws IllegalArgumentException if there are no failures in the results set
   * @deprecated use {@link Result#failure(Result, Result, Result[])}
   */
  @Deprecated
  public static <T> Result<T> propagateFailures(Result<?> result1, Result<?> result2, Result<?>... results) {
    return Result.failure(result1, result2, results);
  }

  /**
   * @deprecated use {@link Result#failure(Iterable)}
   */
  @Deprecated
  public static <T> Result<T> propagateFailures(Collection<Result<?>> results) {
    return Result.failure(results);
  }
}
