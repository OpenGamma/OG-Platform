/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MertonJumpDiffusionModelDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _gamma;

  public MertonJumpDiffusionModelDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double lambda, final double gamma) {
    super(discountCurve, b, volatilitySurface, spot, date);
    ArgumentChecker.notZero(lambda, 1e-15, "lambda");
    _lambda = lambda;
    _gamma = gamma;
  }

  public MertonJumpDiffusionModelDataBundle(final MertonJumpDiffusionModelDataBundle data) {
    super(data);
    ArgumentChecker.notZero(data.getLambda(), 1e-15, "lambda");
    _lambda = data.getLambda();
    _gamma = data.getGamma();
  }

  public MertonJumpDiffusionModelDataBundle(final StandardOptionDataBundle data, final double lambda, final double gamma) {
    super(data);
    ArgumentChecker.notZero(lambda, 1e-15, "lambda");
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
  public MertonJumpDiffusionModelDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new MertonJumpDiffusionModelDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withCostOfCarry(final double costOfCarry) {
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withDate(final ZonedDateTime date) {
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getLambda(), getGamma());
  }

  @Override
  public MertonJumpDiffusionModelDataBundle withSpot(final double spot) {
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getLambda(), getGamma());
  }

  public MertonJumpDiffusionModelDataBundle withLambda(final double lambda) {
    ArgumentChecker.notZero(lambda, 1e-15, "lambda");
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda, getGamma());
  }

  public MertonJumpDiffusionModelDataBundle withGamma(final double gamma) {
    return new MertonJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), gamma);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_gamma);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
