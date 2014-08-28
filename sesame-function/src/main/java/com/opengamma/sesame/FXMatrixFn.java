/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.cache.CacheLifetime;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.sesame.component.CurrencyPairSet;
import com.opengamma.sesame.function.Output;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Function capable of providing an FX Matrix.
 */
public interface FXMatrixFn {

  /**
   * Finds an FX matrix for a set of currencies. Returns a Result.FAILURE when a conversion rate is missing.
   * 
   * @param env evaluation environment
   * @param currencies  the currencies, not null
   * @return the FX matrix, a failure result if not found
   */
  @Cacheable(CacheLifetime.FOREVER)
  @Output(OutputNames.FX_MATRIX)
  Result<FXMatrix> getFXMatrix(Environment env, Set<Currency> currencies);

  /**
   * Finds an FX matrix for a set of currencies.
   * 
   * @param env evaluation environment
   * @param configuration  the curve construction configuration, not null
   * @return the FX matrix, a failure result if not found
   */
  @Cacheable(CacheLifetime.FOREVER)
  @Output(OutputNames.FX_MATRIX)
  Result<FXMatrix> getFXMatrix(Environment env, CurveConstructionConfiguration configuration);

  /**
   * Finds all FX rates available for the set of currency pairs.
   *
   * @param env evaluation environment
   * @param currencyPairs the set of currency pairs, not null
   * @return the available FX rates
   */
  @Cacheable(CacheLifetime.FOREVER)
  @Output(OutputNames.AVAILABLE_FX_RATES)
  Result<Map<Currency, FXMatrix>> getAvailableFxRates(Environment env, CurrencyPairSet currencyPairs);

}
