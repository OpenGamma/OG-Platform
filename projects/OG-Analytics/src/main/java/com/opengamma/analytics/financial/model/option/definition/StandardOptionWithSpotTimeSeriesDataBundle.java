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
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class StandardOptionWithSpotTimeSeriesDataBundle extends StandardOptionDataBundle {
  private final DoubleTimeSeries<?> _spotTS;

  public StandardOptionWithSpotTimeSeriesDataBundle(final YieldAndDiscountCurve discountCurve, final double b, final VolatilitySurface volatilitySurface, final double spot, final ZonedDateTime date,
      final DoubleTimeSeries<?> spotTS) {
    super(discountCurve, b, volatilitySurface, spot, date);
    _spotTS = spotTS;
  }

  public StandardOptionWithSpotTimeSeriesDataBundle(final StandardOptionWithSpotTimeSeriesDataBundle data) {
    super(data);
    _spotTS = data.getSpotTimeSeries();
  }

  public StandardOptionWithSpotTimeSeriesDataBundle(final StandardOptionDataBundle data, final DoubleTimeSeries<?> spotTS) {
    super(data);
    _spotTS = spotTS;
  }

  public DoubleTimeSeries<?> getSpotTimeSeries() {
    return _spotTS;
  }

  @Override
  public StandardOptionWithSpotTimeSeriesDataBundle withInterestRateCurve(final YieldAndDiscountCurve curve) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(curve, getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionWithSpotTimeSeriesDataBundle withCostOfCarry(final double costOfCarry) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(getInterestRateCurve(), costOfCarry, getVolatilitySurface(), getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionWithSpotTimeSeriesDataBundle withVolatilitySurface(final VolatilitySurface surface) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(getInterestRateCurve(), getCostOfCarry(), surface, getSpot(), getDate(), getSpotTimeSeries());
  }

  @Override
  public StandardOptionWithSpotTimeSeriesDataBundle withDate(final ZonedDateTime date) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), date, getSpotTimeSeries());
  }

  @Override
  public StandardOptionWithSpotTimeSeriesDataBundle withSpot(final double spot) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), spot, getDate(), getSpotTimeSeries());
  }

  public StandardOptionWithSpotTimeSeriesDataBundle withSpotTimeSeries(final DoubleTimeSeries<?> spotTS) {
    return new StandardOptionWithSpotTimeSeriesDataBundle(getInterestRateCurve(), getCostOfCarry(), getVolatilitySurface(), getSpot(), getDate(), spotTS);
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
    final StandardOptionWithSpotTimeSeriesDataBundle other = (StandardOptionWithSpotTimeSeriesDataBundle) obj;
    return ObjectUtils.equals(_spotTS, other._spotTS);
  }
}
