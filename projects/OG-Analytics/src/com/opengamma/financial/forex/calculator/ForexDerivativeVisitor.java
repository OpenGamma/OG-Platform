/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;

/**
 * Interface to Forex derivative visitor.
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface ForexDerivativeVisitor<S, T> {

  T visit(ForexDerivative derivative, S data);

  T visit(ForexDerivative derivative);

  T[] visit(ForexDerivative[] derivative, S data);

  T[] visit(ForexDerivative[] derivative);

  T visitForex(Forex derivative, S data);

  T visitForex(Forex derivative);

}
