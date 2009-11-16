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
  private final double _annualizedPearsonKurtosis;

  public SkewKurtosisOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double annualizedSkew, final double annualizedPearsonKurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _annualizedSkew = annualizedSkew;
    _annualizedPearsonKurtosis = annualizedPearsonKurtosis;
  }

  public double getAnnualizedSkew() {
    return _annualizedSkew;
  }

  public double getAnnualizedFischerKurtosis() {
    return _annualizedPearsonKurtosis - 3;
  }

  public double getAnnualizedPearsonKurtosis() {
    return _annualizedPearsonKurtosis;
  }

  @Override
  public SkewKurtosisOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final Double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getAnnualizedSkew(), getAnnualizedPearsonKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final Double spot) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getAnnualizedSkew(), getAnnualizedFischerKurtosis());
  }

  public SkewKurtosisOptionDataBundle withSkew(final Double skew) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), skew, getAnnualizedPearsonKurtosis());
  }

  public SkewKurtosisOptionDataBundle withKurtosis(final Double kurtosis) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualizedSkew(), kurtosis);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_annualizedPearsonKurtosis);
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
    if (Double.doubleToLongBits(_annualizedPearsonKurtosis) != Double.doubleToLongBits(other._annualizedPearsonKurtosis))
      return false;
    if (Double.doubleToLongBits(_annualizedSkew) != Double.doubleToLongBits(other._annualizedSkew))
      return false;
    return true;
  }
}
