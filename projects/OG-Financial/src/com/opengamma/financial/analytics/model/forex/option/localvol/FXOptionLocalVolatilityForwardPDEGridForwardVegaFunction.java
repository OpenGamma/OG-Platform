/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDECalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDEVolatilityGreeksGridCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.PDELocalVolatilityCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class FXOptionLocalVolatilityForwardPDEGridForwardVegaFunction extends FXOptionLocalVolatilityForwardPDEFunction {

  public FXOptionLocalVolatilityForwardPDEGridForwardVegaFunction(final String blackSmileInterpolatorName) {
    super(blackSmileInterpolatorName);
  }

  @Override
  protected String getRequirementName() {
    return ValueRequirementNames.GRID_FORWARD_VEGA;
  }

  @Override
  protected PDELocalVolatilityCalculator<?> getPDECalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    return new LocalVolatilityForwardPDEVolatilityGreeksGridCalculator.VegaCalculator(pdeCalculator, interpolator);
  }

  @Override
  protected Object getResult(final PDELocalVolatilityCalculator<?> calculator, final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve,
      final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    final Interpolator1DDataBundle data = (Interpolator1DDataBundle) calculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
    return InterpolatedDoublesCurve.from(data.getKeys(), data.getValues(), ((LocalVolatilityForwardPDEVolatilityGreeksGridCalculator) calculator).getInterpolator());
  }
}
