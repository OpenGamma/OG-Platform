/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

/**
 * 
 * @param <T> The type of the abscissas
 * General interface to access interest rates.
 * 
 */

public interface InterestRateModel<T> {

  /**
   * 
   * @param x
   *          Abscissa value(s)
   * @return Interest rate as a decimal (i.e. 3% = 0.03)
   * @throws Exception
   */
  double getInterestRate(T x);

}
