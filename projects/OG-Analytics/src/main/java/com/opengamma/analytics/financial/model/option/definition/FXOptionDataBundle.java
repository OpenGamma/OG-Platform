/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class FXOptionDataBundle extends StandardOptionDataBundle {
  private final YieldAndDiscountCurve _foreignInterestRate;

  public FXOptionDataBundle(final YieldAndDiscountCurve domesticInterestRate, final YieldAndDiscountCurve foreignInterestRate, final VolatilitySurface volatilitySurface, final double spot,
      final ZonedDateTime date) {
    super(domesticInterestRate, 0, volatilitySurface, spot, date);
    _foreignInterestRate = foreignInterestRate;
  }

  public YieldAndDiscountCurve getForeignInterestRateCurve() {
    return _foreignInterestRate;
  }

  @Override
  public double getCostOfCarry() {
    final double t = 0; //TODO this only works if the yield curve is constant - need to change when we have a cost of carry model
    return getInterestRate(t) - getForeignInterestRate(t);
  }

  public double getForeignInterestRate(final double t) {
    return _foreignInterestRate.getInterestRate(t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_foreignInterestRate == null) ? 0 : _foreignInterestRate.hashCode());
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
    final FXOptionDataBundle other = (FXOptionDataBundle) obj;
    return ObjectUtils.equals(_foreignInterestRate, other._foreignInterestRate);
  }
}
