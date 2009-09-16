package com.opengamma.financial.model.volatility;

import java.util.Map;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.regression.LeastSquaresRegression;

public class PractitionerBlackScholesVolatilityModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private final VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private LeastSquaresRegression _regression;

  // TODO
  @Override
  public VolatilitySurface getSurface(EuropeanVanillaOptionDefinition definition, Map<EuropeanVanillaOptionDefinition, Double> prices, StandardOptionDataBundle data)
      throws OptionPricingException {
    return null;
  }
}
