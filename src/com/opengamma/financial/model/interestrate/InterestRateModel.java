/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

/**
 * 
 * General interface to access interest rates.
 * 
 * @author emcleod
 */

public interface InterestRateModel<T> {

  /**
   * 
   * @param x
   *          Abscissa value(s)
   * @return Interest rate as a decimal (i.e. 3% = 0.03)
   * @throws Exception
   */
  public double getInterestRate(T x) throws Exception;

}
