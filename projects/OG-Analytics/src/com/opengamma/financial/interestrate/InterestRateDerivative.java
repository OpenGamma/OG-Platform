/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
