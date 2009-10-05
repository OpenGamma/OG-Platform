package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 * 
 */

public class BatesGeneralizedJumpDiffusionModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _expectedJumpSize;
  private final double _delta;

  public BatesGeneralizedJumpDiffusionModelOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date, final double lambda, final double expectedJumpSize, final double delta) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_delta);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_expectedJumpSize);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final BatesGeneralizedJumpDiffusionModelOptionDataBundle other = (BatesGeneralizedJumpDiffusionModelOptionDataBundle) obj;
    if (Double.doubleToLongBits(_delta) != Double.doubleToLongBits(other._delta))
      return false;
    if (Double.doubleToLongBits(_expectedJumpSize) != Double.doubleToLongBits(other._expectedJumpSize))
      return false;
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda))
      return false;
    return true;
  }
}
