/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class BatesGeneralizedJumpDiffusionModelDataBundle extends StandardOptionDataBundle {
  private final double _lambda;
  private final double _expectedJumpSize;
  private final double _delta;

  public BatesGeneralizedJumpDiffusionModelDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date, final double lambda, final double expectedJumpSize, final double delta) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _lambda = lambda;
    _expectedJumpSize = expectedJumpSize;
    _delta = delta;
  }

  public BatesGeneralizedJumpDiffusionModelDataBundle(final BatesGeneralizedJumpDiffusionModelDataBundle data) {
    super(data);
    _lambda = data.getLambda();
    _expectedJumpSize = data.getExpectedJumpSize();
    _delta = data.getDelta();
  }

  public BatesGeneralizedJumpDiffusionModelDataBundle(final StandardOptionDataBundle data, final double lambda, final double expectedJumpSize, final double delta) {
    super(data);
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
  public BatesGeneralizedJumpDiffusionModelDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), getExpectedJumpSize(), getDelta());
  }

  @Override
  public BatesGeneralizedJumpDiffusionModelDataBundle withCostOfCarry(final double costOfCarry) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getLambda(), getExpectedJumpSize(), getDelta());
  }

  @Override
  public BatesGeneralizedJumpDiffusionModelDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getLambda(), getExpectedJumpSize(), getDelta());
  }

  @Override
  public BatesGeneralizedJumpDiffusionModelDataBundle withDate(final ZonedDateTime date) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getLambda(), getExpectedJumpSize(), getDelta());
  }

  @Override
  public BatesGeneralizedJumpDiffusionModelDataBundle withSpot(final double spot) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getLambda(), getExpectedJumpSize(), getDelta());
  }

  public BatesGeneralizedJumpDiffusionModelDataBundle withLambda(final double lambda) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), lambda, getExpectedJumpSize(), getDelta());
  }

  public BatesGeneralizedJumpDiffusionModelDataBundle withExpectedJumpSize(final double expectedJumpSize) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), expectedJumpSize, getDelta());
  }

  public BatesGeneralizedJumpDiffusionModelDataBundle withDelta(final double delta) {
    return new BatesGeneralizedJumpDiffusionModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getLambda(), getExpectedJumpSize(), delta);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_delta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_expectedJumpSize);
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
    final BatesGeneralizedJumpDiffusionModelDataBundle other = (BatesGeneralizedJumpDiffusionModelDataBundle) obj;
    if (Double.doubleToLongBits(_delta) != Double.doubleToLongBits(other._delta)) {
      return false;
    }
    if (Double.doubleToLongBits(_expectedJumpSize) != Double.doubleToLongBits(other._expectedJumpSize)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda)) {
      return false;
    }
    return true;
  }
}
