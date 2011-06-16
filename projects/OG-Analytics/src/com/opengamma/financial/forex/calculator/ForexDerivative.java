/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

/**
 * Interface to Forex derivative accepting a visitor.
 */
public interface ForexDerivative {

  <S, T> T accept(ForexDerivativeVisitor<S, T> visitor, S data);

  <T> T accept(ForexDerivativeVisitor<?, T> visitor);
}
