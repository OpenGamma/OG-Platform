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
  private final double _onePeriodSkew;
  private final double _onePeriodKurtosis;
  private final double _periodsPerYear;

  public SkewKurtosisOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double onePeriodSkew, final double onePeriodKurtosis, final double periodsPerYear) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _onePeriodSkew = onePeriodSkew;
    _onePeriodKurtosis = onePeriodKurtosis;
    _periodsPerYear = periodsPerYear;
  }

  public double getOnePeriodSkew() {
    return _onePeriodSkew;
  }

  public double getOnePeriodKurtosis() {
    return _onePeriodKurtosis;
  }

  public double getPeriodsPerYear() {
    return _periodsPerYear;
  }

  @Override
  public SkewKurtosisOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getOnePeriodSkew(), getOnePeriodKurtosis(), getPeriodsPerYear());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getOnePeriodSkew(), getOnePeriodKurtosis(), getPeriodsPerYear());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getOnePeriodSkew(), getOnePeriodKurtosis(), getPeriodsPerYear());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getOnePeriodSkew(), getOnePeriodKurtosis(), getPeriodsPerYear());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final Double spot) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getOnePeriodSkew(), getOnePeriodKurtosis(), getPeriodsPerYear());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_onePeriodKurtosis);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_onePeriodSkew);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_periodsPerYear);
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
    if (Double.doubleToLongBits(_onePeriodKurtosis) != Double.doubleToLongBits(other._onePeriodKurtosis))
      return false;
    if (Double.doubleToLongBits(_onePeriodSkew) != Double.doubleToLongBits(other._onePeriodSkew))
      return false;
    if (Double.doubleToLongBits(_periodsPerYear) != Double.doubleToLongBits(other._periodsPerYear))
      return false;
    return true;
  }
}
