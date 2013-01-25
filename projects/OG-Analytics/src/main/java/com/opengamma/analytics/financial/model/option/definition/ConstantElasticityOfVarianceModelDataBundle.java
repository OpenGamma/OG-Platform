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
public class ConstantElasticityOfVarianceModelDataBundle extends StandardOptionDataBundle {
  private final double _elasticity;

  public ConstantElasticityOfVarianceModelDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double elasticity) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _elasticity = elasticity;
  }

  public ConstantElasticityOfVarianceModelDataBundle(final StandardOptionDataBundle data, final double elasticity) {
    super(data);
    _elasticity = elasticity;
  }

  public double getElasticity() {
    return _elasticity;
  }

  @Override
  public ConstantElasticityOfVarianceModelDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new ConstantElasticityOfVarianceModelDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getElasticity());
  }

  @Override
  public ConstantElasticityOfVarianceModelDataBundle withCostOfCarry(final double costOfCarry) {
    return new ConstantElasticityOfVarianceModelDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getElasticity());
  }

  @Override
  public ConstantElasticityOfVarianceModelDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new ConstantElasticityOfVarianceModelDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getElasticity());
  }

  @Override
  public ConstantElasticityOfVarianceModelDataBundle withDate(final ZonedDateTime date) {
    return new ConstantElasticityOfVarianceModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getElasticity());
  }

  @Override
  public ConstantElasticityOfVarianceModelDataBundle withSpot(final double spot) {
    return new ConstantElasticityOfVarianceModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getElasticity());
  }

  public ConstantElasticityOfVarianceModelDataBundle withElasticity(final double elasticity) {
    return new ConstantElasticityOfVarianceModelDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), elasticity);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_elasticity);
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
    final ConstantElasticityOfVarianceModelDataBundle other = (ConstantElasticityOfVarianceModelDataBundle) obj;
    if (Double.doubleToLongBits(_elasticity) != Double.doubleToLongBits(other._elasticity)) {
      return false;
    }
    return true;
  }

}
