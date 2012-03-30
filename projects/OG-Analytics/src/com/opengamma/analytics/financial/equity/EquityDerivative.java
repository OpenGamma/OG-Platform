/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

/**
 * 
 */
public interface EquityDerivative {

  <S, T> T accept(EquityDerivativeVisitor<S, T> visitor, S data);

  <T> T accept(EquityDerivativeVisitor<?, T> visitor);
}
