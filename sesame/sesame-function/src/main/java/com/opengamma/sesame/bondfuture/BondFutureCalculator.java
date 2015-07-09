/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Bond future calculator.
 */
public interface BondFutureCalculator {

  /**
   * Calculate the PV for a bond future security.
   * @return result containing the PV if successful, a Failure otherwise.
   */
  Result<MultipleCurrencyAmount> calculatePV();

  /**
   * Calculate the PV01 for a bond future security.
   * @return result containing the PV01 if successful, a Failure otherwise.
   */
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01();

  /**
   * Calculate the Bucketed PV01 for a bond future security.
   * @return result containing the bucketed PV01 if successful, a Failure otherwise.
   */
  Result<MultipleCurrencyParameterSensitivity> calculateBucketedPV01();

  /**
   * Calculate the model price of the bond future option.
   * @return the model price of the bond future option.
   */
  Result<Double> calculateSecurityModelPrice();

}
