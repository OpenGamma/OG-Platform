/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class StandardOptionDataBundleWithSpotTimeSeries extends StandardOptionDataBundle {
  private final DoubleTimeSeries _spotTS;

  public StandardOptionDataBundleWithSpotTimeSeries(DiscountCurve discountCurve, double b, VolatilitySurface volatilitySurface, double spot, ZonedDateTime date,
      DoubleTimeSeries spotTS) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _spotTS = spotTS;
  }

  public DoubleTimeSeries getSpotTimeSeries() {
    return _spotTS;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_spotTS == null) ? 0 : _spotTS.hashCode());
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
    StandardOptionDataBundleWithSpotTimeSeries other = (StandardOptionDataBundleWithSpotTimeSeries) obj;
    if (_spotTS == null) {
      if (other._spotTS != null)
        return false;
    } else if (!_spotTS.equals(other._spotTS))
      return false;
    return true;
  }
}
