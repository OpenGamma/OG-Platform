/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.util.result.Result;

/**
 * Converts an {@link InterestRateSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public interface InterestRateSwapConverterFn {

  /**
   * Converts a swap security to a swap definition.
   *
   * @param env the environment for the calculations
   * @param security the security
   * @return a definition derived from the security
   */
  @Cacheable
  Result<SwapDefinition> createDefinition(Environment env, InterestRateSwapSecurity security);

  /**
   * Converts a swap definition to an instrument derivative.
   *
   * @param env the environment for the calculations
   * @param security the security
   * @param definition a definition derived from the security
   * @param fixings time series of fixings for the security
   * @return instrument derivative created from the security
   */
  @Cacheable
  Result<InstrumentDerivative> createDerivative(Environment env,
                                                InterestRateSwapSecurity security,
                                                SwapDefinition definition,
                                                HistoricalTimeSeriesBundle fixings);
}
