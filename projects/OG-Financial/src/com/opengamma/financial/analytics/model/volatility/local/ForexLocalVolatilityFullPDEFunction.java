/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.local.LocalVolatilityForwardPDEGreekCalculator;
import com.opengamma.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;

/**
 * 
 */
public class ForexLocalVolatilityFullPDEFunction extends ForexLocalVolatilityPDEGridFunction {

  @Override
  protected Object getResult(final LocalVolatilityForwardPDEGreekCalculator<?> calculator, final LocalVolatilitySurface<?> localVolatilitySurface,
      final ForwardCurve forwardCurve, final SmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    return calculator.solve(data, localVolatilitySurface);
  }

  @Override
  protected String getResultName() {
    return ValueRequirementNames.LOCAL_VOLATILITY_FULL_PDE_GRID;
  }
}
