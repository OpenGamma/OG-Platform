/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Converts an {@link InterestRateSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public interface InterestRateSwapConverterFn {

  /**
   * Converts a swap security to a swap definition and an instrument derivative.
   *
   * @param env the environment for the calculations
   * @param security the security
   * @return swap definition and instrument derivative created from the security
   */
  Result<Pair<SwapDefinition, InstrumentDerivative>> convert(Environment env, InterestRateSwapSecurity security);
}
