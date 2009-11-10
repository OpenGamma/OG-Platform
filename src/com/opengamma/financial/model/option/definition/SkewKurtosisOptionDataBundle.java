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
  private final double _annualisedSkew;
  private final double _annualisedKurtosis;

  public SkewKurtosisOptionDataBundle(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final double annualisedSkew, final double annualisedKurtosis) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _annualisedSkew = annualisedSkew;
    _annualisedKurtosis = annualisedKurtosis;
  }

  public double getAnnualisedSkew() {
    return _annualisedSkew;
  }

  public double getAnnualisedKurtosis() {
    return _annualisedKurtosis;
  }

  @Override
  public SkewKurtosisOptionDataBundle withDiscountCurve(final DiscountCurve curve) {
    return new SkewKurtosisOptionDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualisedSkew(), getAnnualisedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withCostOfCarry(final double costOfCarry) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getAnnualisedSkew(), getAnnualisedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getAnnualisedSkew(), getAnnualisedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withDate(final ZonedDateTime date) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getAnnualisedSkew(), getAnnualisedKurtosis());
  }

  @Override
  public SkewKurtosisOptionDataBundle withSpot(final Double spot) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getAnnualisedSkew(), getAnnualisedKurtosis());
  }

  public SkewKurtosisOptionDataBundle withSkew(final Double skew) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), skew, getAnnualisedKurtosis());
  }

  public SkewKurtosisOptionDataBundle withKurtosis(final Double kurtosis) {
    return new SkewKurtosisOptionDataBundle(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getAnnualisedSkew(), kurtosis);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_annualisedKurtosis);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_annualisedSkew);
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
    if (Double.doubleToLongBits(_annualisedKurtosis) != Double.doubleToLongBits(other._annualisedKurtosis))
      return false;
    if (Double.doubleToLongBits(_annualisedSkew) != Double.doubleToLongBits(other._annualisedSkew))
      return false;
    return true;
  }
}
