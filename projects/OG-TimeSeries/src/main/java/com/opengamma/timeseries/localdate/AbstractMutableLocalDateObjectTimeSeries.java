/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * Partial implementation of the {@link MutableLocalDateObjectTimeSeries} that uses
 * an {@code int} representation of the date.
 * 
 * @param <T>  the type of the data
 */
public abstract class AbstractMutableLocalDateObjectTimeSeries<T>
    extends AbstractMutableIntObjectTimeSeries<LocalDate, T>
    implements MutableLocalDateObjectTimeSeries<T> {

  /** Serialization version. */
  private static final long serialVersionUID = 8844943979803402189L;

  /**
   * Creates an instance.
   * 
   * @param converter  the converter to use, not null
   * @param timeSeries  the underlying time-series, not null
   */
  public AbstractMutableLocalDateObjectTimeSeries(final DateTimeConverter<LocalDate> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
    super(converter, timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public ObjectTimeSeries<LocalDate, T> newInstance(final LocalDate[] dateTimes, final T[] values) {
    return newInstanceFast(dateTimes, values);
  }

  public abstract LocalDateObjectTimeSeries<T> newInstanceFast(LocalDate[] dateTimes, T[] values);

}
