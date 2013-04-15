/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ExtremeSpreadOptionDefinition extends OptionDefinition {
  private final OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionWithSpotTimeSeriesDataBundle data, final Double optionPrice) {
      Validate.notNull(data, "data");
      final DoubleTimeSeries<ZonedDateTime> ts = ImmutableZonedDateTimeDoubleTimeSeries.of((PreciseDoubleTimeSeries<?>) data.getSpotTimeSeries(), ZoneOffset.UTC);
      final ZonedDateTime periodEnd = getPeriodEnd().getExpiry();
      final DoubleTimeSeries<ZonedDateTime> firstPeriod = ts.subSeries(data.getDate(), true, periodEnd, true);
      final DoubleTimeSeries<ZonedDateTime> secondPeriod = ts.subSeries(periodEnd, false, ts.getLatestTime(), true);
      if (isCall()) {
        return isReverse() ? Math.abs(secondPeriod.minValue() - firstPeriod.minValue()) : Math.abs(secondPeriod.maxValue() - firstPeriod.maxValue());
      }
      return isReverse() ? Math.abs(secondPeriod.maxValue() - firstPeriod.maxValue()) : Math.abs(secondPeriod.minValue() - firstPeriod.minValue());
    }
  };
  private final Expiry _periodEnd;
  private final boolean _isReverse;

  public ExtremeSpreadOptionDefinition(final Expiry expiry, final boolean isCall, final Expiry periodEnd, final boolean isReverse) {
    super(null, expiry, isCall);
    Validate.notNull(periodEnd, "period end");
    if (expiry.getExpiry().isBefore(periodEnd.getExpiry())) {
      throw new IllegalArgumentException("Period end must be before option expiry");
    }
    _periodEnd = periodEnd;
    _isReverse = isReverse;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  public double getTimeFromPeriodEnd(final ZonedDateTime date) {
    Validate.notNull(date, "date");
    return DateUtils.getDifferenceInYears(_periodEnd.getExpiry(), date);
  }

  public Expiry getPeriodEnd() {
    return _periodEnd;
  }

  public boolean isReverse() {
    return _isReverse;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isReverse ? 1231 : 1237);
    result = prime * result + ((_periodEnd == null) ? 0 : _periodEnd.hashCode());
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
    final ExtremeSpreadOptionDefinition other = (ExtremeSpreadOptionDefinition) obj;
    return ObjectUtils.equals(_periodEnd, other._periodEnd) && _isReverse == other._isReverse;
  }
}
