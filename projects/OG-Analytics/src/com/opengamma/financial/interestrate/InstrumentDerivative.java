/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

/**
 * 
 */
public interface InstrumentDerivative {

  <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data);

  <T> T accept(InstrumentDerivativeVisitor<?, T> visitor);
}
