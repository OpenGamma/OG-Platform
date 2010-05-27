/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class StandardOptionDataBundleWithSpotTimeSeries extends StandardOptionDataBundle {
  private final DoubleTimeSeries<?> _spotTS;

  public StandardOptionDataBundleWithSpotTimeSeries(final DiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final DoubleTimeSeries<?> spotTS) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _spotTS = spotTS;
  }

  public StandardOptionDataBundleWithSpotTimeSeries(final StandardOptionDataBundleWithSpotTimeSeries data) {
    super(data);
    _spotTS = data.getSpotTimeSeries();
  }

  public DoubleTimeSeries<?> getSpotTimeSeries() {
    return _spotTS;
  }

  @Override
  public StandardOptionDataBundleWithSpotTimeSeries withDiscountCurve(final DiscountCurve curve) {
    return new StandardOptionDataBundleWithSpotTimeSeries(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionDataBundleWithSpotTimeSeries withCostOfCarry(final Double costOfCarry) {
    return new StandardOptionDataBundleWithSpotTimeSeries(getDiscountCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionDataBundleWithSpotTimeSeries withVolatilitySurface(final VolatilitySurface surface) {
    return new StandardOptionDataBundleWithSpotTimeSeries(getDiscountCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionDataBundleWithSpotTimeSeries withDate(final ZonedDateTime date) {
    return new StandardOptionDataBundleWithSpotTimeSeries(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getSpotTimeSeries());
  }

  @Override
  public StandardOptionDataBundleWithSpotTimeSeries withSpot(final Double spot) {
    return new StandardOptionDataBundleWithSpotTimeSeries(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getSpotTimeSeries());
  }

  public StandardOptionDataBundleWithSpotTimeSeries withSpotTimeSeries(final DoubleTimeSeries<?> spotTS) {
    return new StandardOptionDataBundleWithSpotTimeSeries(getDiscountCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), spotTS);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_spotTS == null ? 0 : _spotTS.hashCode());
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
    final StandardOptionDataBundleWithSpotTimeSeries other = (StandardOptionDataBundleWithSpotTimeSeries) obj;
    if (_spotTS == null) {
      if (other._spotTS != null) {
        return false;
      }
    } else if (!_spotTS.equals(other._spotTS)) {
      return false;
    }
    return true;
  }
}
