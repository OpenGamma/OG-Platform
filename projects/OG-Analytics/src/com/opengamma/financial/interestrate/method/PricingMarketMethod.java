/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.MarketBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Interface for interest rate pricing methods.
 */
public interface PricingMarketMethod {

  /**
   * Computes the present value of an instrument.
   * @param instrument The instrument.
   * @param market The curves market.
   * @return The present value.
   */
  CurrencyAmount presentValue(final InterestRateDerivative instrument, final MarketBundle market);

}
