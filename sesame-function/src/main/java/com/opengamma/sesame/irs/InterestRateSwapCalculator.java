/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculator initialised with the data required to perform
 * analytics calculations for a particular security.
 */
public interface InterestRateSwapCalculator {

  /**
   * Calculates the present value for the security
   *
   * @return the present value
   */
  Result<MultipleCurrencyAmount> calculatePV();

  /**
   * Calculates the par rate for the security
   *
   * @return the par rate
   */
  Result<Double> calculateRate();

  /**
   * Calculates the PV01 for the security
   *
   * @return the PV01
   */
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01();
}
