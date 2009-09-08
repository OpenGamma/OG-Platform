package com.opengamma.math.function;

/**
 * 
 * @author emcleod
 * 
 *         Many functions only need one argument: extending this function
 *         eliminates the need to create an array.
 * @param <S>
 *          Type of the arguments
 * @param <T>
 *          Return type of function
 */
public abstract class Function1D<S, T> implements Function<S, T> {

  public T evaluate(S... x) {
    if (x == null)
      throw new IllegalArgumentException("Null argument");
    return evaluate(x[0]);
  }

  public abstract T evaluate(S x);
}
