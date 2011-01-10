/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

/**
 * 
 */
//TODO yuck yuck yuck
public interface InterestRateDerivativeWithRate extends InterestRateDerivative {

  InterestRateDerivativeWithRate withRate(double rate);
}
