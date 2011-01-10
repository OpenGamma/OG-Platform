/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

/**
 * 
 */
public interface InterestRateDerivative {

  <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data);

  <T> T accept(InterestRateDerivativeVisitor<?, T> visitor);
}
