/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 * @author emcleod
 */
public class MertonJumpDiffusionModelOptionDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _gamma;

  public MertonJumpDiffusionModelOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date, final double lambda, final double gamma) {
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

  @Override
  public MertonJumpDiffusionModelOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new MertonJumpDiffusionModelOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelOptionDataBundle withCostOfCarry(final Double costOfCarry) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelOptionDataBundle withDate(final ZonedDateTime date) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelOptionDataBundle withSpot(final Double spot) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getLambda(), getGamma());
  }

  public MertonJumpDiffusionModelOptionDataBundle withLambda(final Double lambda) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda, getGamma());
  }

  public MertonJumpDiffusionModelOptionDataBundle withGamma(final Double gamma) {
    return new MertonJumpDiffusionModelOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), gamma);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_gamma);
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
    final MertonJumpDiffusionModelOptionDataBundle other = (MertonJumpDiffusionModelOptionDataBundle) obj;
    if (Double.doubleToLongBits(_gamma) != Double.doubleToLongBits(other._gamma))
      return false;
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda))
      return false;
    return true;
  }
}
