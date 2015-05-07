/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Converts an {@link ZeroCouponInflationSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public interface InflationSwapConverterFn {

  /**
   * Converts a ZC Inflation Swap Security to a swap definition and an instrument derivative.
   *
   * @param env the environment for the calculations
   * @param security the security
   * @return swap definition and instrument derivative created from the security
   */
  Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> convert(Environment env, ZeroCouponInflationSwapSecurity security);
}
