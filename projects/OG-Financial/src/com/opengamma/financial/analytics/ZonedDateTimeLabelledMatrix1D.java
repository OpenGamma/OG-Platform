/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class ZonedDateTimeLabelledMatrix1D extends LabelledMatrix1D<ZonedDateTime, Period> {
  private static final Period TOLERANCE = Period.ofNanos(1);

  public ZonedDateTimeLabelledMatrix1D(final ZonedDateTime[] keys, final double[] values) {
    super(keys, values, TOLERANCE);
  }

  public ZonedDateTimeLabelledMatrix1D(final ZonedDateTime[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, TOLERANCE);
  }

  @Override
  public int compare(final ZonedDateTime d1, final ZonedDateTime d2, final Period tolerance) {
    final ZonedDateTime dLow = d1.minus(tolerance);
    final ZonedDateTime dHigh = d1.plus(tolerance);
    if (d1.equals(d2) || (d2.isAfter(dLow) && d2.isBefore(dHigh))) {
      return 0;
    }
    return d1.isBefore(d2) ? -1 : 1;

  }

  @Override
  protected LabelledMatrix1D<ZonedDateTime, Period> getMatrix(final ZonedDateTime[] keys, final Object[] labels, final double[] values) {
    return new ZonedDateTimeLabelledMatrix1D(keys, labels, values);
  }

  @Override
  protected LabelledMatrix1D<ZonedDateTime, Period> getMatrix(final ZonedDateTime[] keys, final double[] values) {
    return new ZonedDateTimeLabelledMatrix1D(keys, values);
  }

}
