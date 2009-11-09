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
  private final double _skew;
  private final double _kurtosis;

  public SkewKurtosisOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double skew, final double kurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _skew = skew;
    _kurtosis = kurtosis;
  }

  public double getSkew() {
    return _skew;
  }

  public double getKurtosis() {
    return _kurtosis;
  }

  @Override
  public SkewKurtosisOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getSkew(), getKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getSkew(), getKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getSkew(), getKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getSkew(), getKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final Double spot) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getSkew(), getKurtosis());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_kurtosis);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_skew);
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
    if (Double.doubleToLongBits(_kurtosis) != Double.doubleToLongBits(other._kurtosis))
      return false;
    if (Double.doubleToLongBits(_skew) != Double.doubleToLongBits(other._skew))
      return false;
    return true;
  }
}
