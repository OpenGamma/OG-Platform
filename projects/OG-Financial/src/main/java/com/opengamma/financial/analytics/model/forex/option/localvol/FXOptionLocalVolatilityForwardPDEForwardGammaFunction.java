/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDECalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDESingleResultCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDESpotGreeksGridCalculator;
import com.opengamma.analytics.financial.model.volatility.local.PDELocalVolatilityCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class FXOptionLocalVolatilityForwardPDEForwardGammaFunction extends FXOptionLocalVolatilityForwardPDEFunction {

  public FXOptionLocalVolatilityForwardPDEForwardGammaFunction(final String blackSmileInterpolatorName) {
    super(blackSmileInterpolatorName);
  }

  @Override
  protected String getRequirementName() {
    return ValueRequirementNames.FORWARD_GAMMA;
  }

  @Override
  protected PDELocalVolatilityCalculator<?> getPDECalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    return new LocalVolatilityForwardPDESingleResultCalculator(new LocalVolatilityForwardPDESpotGreeksGridCalculator.GammaCalculator(pdeCalculator, interpolator), interpolator);
  }

}
