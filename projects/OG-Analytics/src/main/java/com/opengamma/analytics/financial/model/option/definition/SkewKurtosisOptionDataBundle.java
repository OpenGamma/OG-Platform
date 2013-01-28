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
public class SkewKurtosisOptionDataBundle extends StandardOptionDataBundle {
  private final double _annualizedSkew;
  private final double _annualizedPearsonKurtosis;
  private final double _annualizedFisherKurtosis;

  public SkewKurtosisOptionDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double annualizedSkew, final double annualizedPearsonKurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _annualizedSkew = annualizedSkew;
    _annualizedPearsonKurtosis = annualizedPearsonKurtosis;
    _annualizedFisherKurtosis = _annualizedPearsonKurtosis - 3;
  }

  public SkewKurtosisOptionDataBundle(final SkewKurtosisOptionDataBundle data) {
    super(data);
    _annualizedSkew = data.getAnnualizedSkew();
    _annualizedPearsonKurtosis = data.getAnnualizedPearsonKurtosis();
    _annualizedFisherKurtosis = _annualizedPearsonKurtosis - 3;
  }

  public SkewKurtosisOptionDataBundle(final StandardOptionDataBundle data, final double annualizedSkew, final double annualizedPearsonKurtosis) {
    super(data);
    _annualizedSkew = annualizedSkew;
    _annualizedPearsonKurtosis = annualizedPearsonKurtosis;
    _annualizedFisherKurtosis = annualizedPearsonKurtosis - 3;
  }

  public double getAnnualizedSkew() {
    return _annualizedSkew;
  }

  public double getAnnualizedFisherKurtosis() {
    return _annualizedFisherKurtosis;
  }

  public double getAnnualizedPearsonKurtosis() {
    return _annualizedPearsonKurtosis;
  }

  @Override
  public SkewKurtosisOptionDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final double spot) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  public SkewKurtosisOptionDataBundle withSkew(final double skew) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), skew, getAnnualizedPearsonKurtosis());
  }

  public SkewKurtosisOptionDataBundle withKurtosis(final double kurtosis) {
    return new SkewKurtosisOptionDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), kurtosis);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_annualizedPearsonKurtosis);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_annualizedSkew);
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
    final SkewKurtosisOptionDataBundle other = (SkewKurtosisOptionDataBundle) obj;
    if (Double.doubleToLongBits(_annualizedPearsonKurtosis) != Double.doubleToLongBits(other._annualizedPearsonKurtosis)) {
      return false;
    }
    if (Double.doubleToLongBits(_annualizedSkew) != Double.doubleToLongBits(other._annualizedSkew)) {
      return false;
    }
    return true;
  }
}
