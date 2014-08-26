/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Left and right extrapolation used with SABR smile interpolation model, 
 * {@link SmileInterpolatorSABRWithExtrapolation}
 */
public abstract class SmileExtrapolationFunctionSABRProvider {

  /**
   * @param sabrDataLow The SABR data for the leftmost subinterval 
   * @param sabrDataHigh The SABR data for the rightmost subinterval. If interpolated globally, 
   * we have sabrDataLow == sabrDataHigh
   * @param volatilityFunction The volatility formula
   * @param forward The forward
   * @param expiry The expiry
   * @param cutOffStrikeLow The left cutoff
   * @param cutOffStrikeHigh The right cutoff
   * @return Volatility function of the extrapolation
   */
  abstract Function1D<Double, Double> getExtrapolationFunction(final SABRFormulaData sabrDataLow,
      final SABRFormulaData sabrDataHigh,
      final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction, final double forward, final double expiry,
      final double cutOffStrikeLow, final double cutOffStrikeHigh);

}
