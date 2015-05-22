/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equityindexoptions;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;

/**
 * Equity index option results.
 */
public interface EquityIndexOptionFn {

  /**
   * Calculates the present value of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the present value of the equity index option.
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<CurrencyAmount> calculatePv(Environment env, EquityIndexOptionTrade trade);

  /**
   * Calculates the delta of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the delta of the equity index option.
   */
  @Output(OutputNames.DELTA)
  Result<Double> calculateDelta(Environment env, EquityIndexOptionTrade trade);

  /**
   * Calculates the gamma of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the gamma of the equity index option.
   */
  @Output(OutputNames.GAMMA)
  Result<Double> calculateGamma(Environment env, EquityIndexOptionTrade trade);

  /**
   * Calculates the vega of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the vega of the equity index option.
   */
  @Output(OutputNames.VEGA)
  Result<Double> calculateVega(Environment env, EquityIndexOptionTrade trade);

  /**
   * Calculates the PV01 of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the PV01 of the equity index option.
   */
  @Output(OutputNames.PV01)
  Result<Double> calculatePv01(Environment env, EquityIndexOptionTrade trade);

  /**
   * Calculates the Bucketed PV01 of the equity index option.
   * @param env the environment, not null.
   * @param trade the equity index option trade, not null.
   * @return the Bucketed PV01 of the equity index option.
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<MultipleCurrencyParameterSensitivity> calculateBucketedPv01(Environment env, EquityIndexOptionTrade trade);

}
