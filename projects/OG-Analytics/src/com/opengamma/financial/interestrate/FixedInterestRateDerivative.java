/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
