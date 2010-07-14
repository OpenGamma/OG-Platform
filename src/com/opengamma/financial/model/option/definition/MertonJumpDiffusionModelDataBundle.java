/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class MertonJumpDiffusionModelDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _gamma;

  public MertonJumpDiffusionModelDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double lambda, final double gamma) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _gamma = gamma;
  }

  public MertonJumpDiffusionModelDataBundle(final MertonJumpDiffusionModelDataBundle data) {
    super(data);
    _lambda = data.getLambda();
    _gamma = data.getGamma();
  }

  public MertonJumpDiffusionModelDataBundle(final StandardOptionDataBundle data, final double lambda, final double gamma) {
    super(data);
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
  public MertonJumpDiffusionModelDataBundle withDiscountCurve(final YieldAndDiscountCurve curve) {
    return new MertonJumpDiffusionModelDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withCostOfCarry(final Double costOfCarry) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withDate(final ZonedDateTime date) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withSpot(final Double spot) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getLambda(), getGamma());
  }

  public MertonJumpDiffusionModelDataBundle withLambda(final Double lambda) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda, getGamma());
  }

  public MertonJumpDiffusionModelDataBundle withGamma(final Double gamma) {
    return new MertonJumpDiffusionModelDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), gamma);
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
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MertonJumpDiffusionModelDataBundle other = (MertonJumpDiffusionModelDataBundle) obj;
    if (Double.doubleToLongBits(_gamma) != Double.doubleToLongBits(other._gamma)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda)) {
      return false;
    }
    return true;
  }
}
