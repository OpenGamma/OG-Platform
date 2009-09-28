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
public class StandardOptionDataBundleWithOptionPrice extends StandardOptionDataBundle {
  private final double _optionPrice;

  public StandardOptionDataBundleWithOptionPrice(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, ZonedDateTime date, double optionPrice) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _optionPrice = optionPrice;
  }

  public double getOptionPrice() {
    return _optionPrice;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_optionPrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    StandardOptionDataBundleWithOptionPrice other = (StandardOptionDataBundleWithOptionPrice) obj;
    if (Double.doubleToLongBits(_optionPrice) != Double.doubleToLongBits(other._optionPrice))
      return false;
    return true;
  }
}
