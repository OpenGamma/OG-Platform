/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fxrates;

import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Obtain fx rates for a given Security.
 */
public interface FxRatesFn {

  /**
   * Calculate the fx rates for security.
   *
   * @param env the environment used for calculation
   * @param security the Security to obtain the relevant currencies
   * @return result containing the fx rates if successful, a Failure otherwise
   */
  @Output(OutputNames.FX_RATES)
  Result<Map<Currency, Double>> getFxRates(Environment env, Security security);

  /**
   * Calculate the fx rates for security.
   *
   * @param env the environment used for calculation
   * @param trade the TradeWrapper to obtain security and then the relevant currencies
   * @return result containing the fx rates if successful, a Failure otherwise
   */
  @Output(OutputNames.FX_RATES)
  Result<Map<Currency, Double>> getFxRates(Environment env, TradeWrapper trade);

}
