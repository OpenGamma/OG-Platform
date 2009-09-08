package com.opengamma.financial.model.option.definition;

import java.util.Date;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 */
public class BatesGeneralizedJumpDiffusionModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _expectedJumpSize;
  private final double _delta;

  public BatesGeneralizedJumpDiffusionModelOptionDataBundle(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, Date date, double lambda, double expectedJumpSize,
      double delta) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _expectedJumpSize = expectedJumpSize;
    _delta = delta;
  }

  public double getLambda() {
    return _lambda;
  }

  public double getExpectedJumpSize() {
    return _expectedJumpSize;
  }

  public double getDelta() {
    return _delta;
  }
}
