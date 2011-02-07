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
    return new Function1D<T, U>() {
      @Override
      public final U evaluate(final T params) {
        return ParameterizedFunction.this.evaluate(x, params);
      }
    };
  }

  public Function1D<S, U> asFunctionOfArguments(final T params) {
    return new Function1D<S, U>() {
      @Override
      public U evaluate(final S x) {
        return ParameterizedFunction.this.evaluate(x, params);
      }
    };
  }

}
