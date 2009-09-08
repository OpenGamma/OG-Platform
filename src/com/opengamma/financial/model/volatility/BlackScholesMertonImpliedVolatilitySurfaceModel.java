package com.opengamma.financial.model.volatility;

import java.util.Date;
import java.util.Map;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.SingleRootFinder;

public class BlackScholesMertonImpliedVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition> {
  private final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  private final SingleRootFinder<Double> _rootFinder = new BisectionSingleRootFinder();
  private final double EPS = 1e-9;

  // TODO
  @Override
  public VolatilitySurface getSurface(EuropeanVanillaOptionDefinition definition, final Map<EuropeanVanillaOptionDefinition, Object[]> data, final Date date) {
    return null;
  }
}