/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function;

/**
 * 
 * @param <S> Type of arguments
 * @param <T> Type of parameters 
 * @param <U> Type of result
 */
public abstract class ParameterizedFunction<S, T, U> {

  public abstract U evaluate(S x, T parameters);

  public Function1D<T, U> asFunctionOfParameters(final S x) {
    Function1D<T, U> res = new Function1D<T, U>() {
      @Override
      public U evaluate(T params) {
        return ParameterizedFunction.this.evaluate(x, params);
      }
    };
    return res;
  }

  public Function1D<S, U> asFuntionOfArguments(final T params) {
    Function1D<S, U> res = new Function1D<S, U>() {
      @Override
      public U evaluate(S x) {
        return ParameterizedFunction.this.evaluate(x, params);
      }
    };
    return res;
  }

}
