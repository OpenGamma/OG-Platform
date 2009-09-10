package com.opengamma.math.function;

/**
 * 
 * @author emcleod
 * 
 *         Interface for function definition. The function arguments can be
 *         multi-dimensional (but not multi-type), as can the function value.
 *         The return type of the function is not necessarily the same as that
 *         of the inputs.
 * @param <S>
 *          Type of the arguments
 * @param <T>
 *          Return type of function
 */
public interface Function<S, T, U extends Exception> {

  /**
   * 
   * @param x
   *          The list of inputs into the function
   * @return The value of the function
   */
  public T evaluate(S... x) throws U;
}
