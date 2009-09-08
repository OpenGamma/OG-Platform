package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 */
public class MertonJumpDiffusionModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _gamma;

  public MertonJumpDiffusionModelOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, Date date, double lambda, double gamma) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _gamma = gamma;
  }

  public double getLambda() {
    return _lambda;
  }

  public double getGamma() {
    return _gamma;
  }
}
