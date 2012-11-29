/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDEGreekCalculator1;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public class ForexLocalVolatilityGridGreeksFunction extends ForexLocalVolatilityPDEGridFunction {

  @Override
  protected Object getResult(final LocalVolatilityForwardPDEGreekCalculator1<?> calculator, final LocalVolatilitySurface<?> localVolatilitySurface, final ForwardCurve forwardCurve,
      final SmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    return calculator.getGridGreeks(data, localVolatilitySurface, option);
  }

  @Override
  protected String getResultName() {
    return ValueRequirementNames.LOCAL_VOLATILITY_PDE_GREEKS;
  }

}
