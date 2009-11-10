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
public class SkewKurtosisOptionDataBundle extends StandardOptionDataBundle {
  private final double _annualizedSkew;
  private final double _annualizedKurtosis;

  public SkewKurtosisOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double annualizedSkew, final double annualizedKurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _annualizedSkew = annualizedSkew;
    _annualizedKurtosis = annualizedKurtosis;
  }

  public double getAnnualizedSkew() {
    return _annualizedSkew;
  }

  public double getAnnualizedKurtosis() {
    return _annualizedKurtosis;
  }

  @Override
  public SkewKurtosisOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getAnnualizedSkew(), getAnnualizedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final Double spot) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getAnnualizedSkew(), getAnnualizedKurtosis());
  }

  public SkewKurtosisOptionDataBundle withSkew(final Double skew) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), skew, getAnnualizedKurtosis());
  }

  public SkewKurtosisOptionDataBundle withKurtosis(final Double kurtosis) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), kurtosis);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_annualizedKurtosis);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_annualizedSkew);
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
    final SkewKurtosisOptionDataBundle other = (SkewKurtosisOptionDataBundle) obj;
    if (Double.doubleToLongBits(_annualizedKurtosis) != Double.doubleToLongBits(other._annualizedKurtosis))
      return false;
    if (Double.doubleToLongBits(_annualizedSkew) != Double.doubleToLongBits(other._annualizedSkew))
      return false;
    return true;
  }
}
