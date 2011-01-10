/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

/**
 * 
 */
public interface FloatingInterestRateDerivative extends InterestRateDerivative {

  double[] getFloatingYearFractions();

  double[] getFloatingPaymentTimes();

  int getNumberOfFloatingPayments();

}
