/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

/**
 * 
 */
public interface FixedInterestRateDerivative extends InterestRateDerivative {

  double[] getFixedYearFractions();

  double[] getFixedPaymentTimes();

  int getNumberOfFixedPayments();

}
