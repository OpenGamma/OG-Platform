/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.regression.LeastSquaresRegression;

public class PractitionerBlackScholesVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private final VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private LeastSquaresRegression _regression;

  // TODO
  @Override
  public VolatilitySurface getSurface(Map<EuropeanVanillaOptionDefinition, Double> prices, StandardOptionDataBundle data) {
    return null;
  }
}
