/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
