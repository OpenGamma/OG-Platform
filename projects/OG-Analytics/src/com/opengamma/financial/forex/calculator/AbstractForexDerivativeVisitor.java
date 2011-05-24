/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;

/**
 * Abstract visitor for Forex derivatives.
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public class AbstractForexDerivativeVisitor<S, T> implements ForexDerivativeVisitor<S, T> {

  @Override
  public T visit(ForexDerivative derivative, S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T visit(ForexDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(ForexDerivative[] derivative, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T[] visit(ForexDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitForex(Forex derivative, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForex(Forex derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

}
