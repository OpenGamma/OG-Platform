/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import org.threeten.bp.Period;

import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class TenorLabelledMatrix1D extends LabelledMatrix1D<Tenor, Period> {
  private static final Period TOLERANCE = Period.ofDays(1);

  public TenorLabelledMatrix1D(final Tenor[] keys, final double[] values) {
    super(keys, values, TOLERANCE);
  }

  public TenorLabelledMatrix1D(final Tenor[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, TOLERANCE);
  }

  public TenorLabelledMatrix1D(final Tenor[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    super(keys, labels, labelsTitle, values, valuesTitle, TOLERANCE);
  }

  @Override
  public int compare(final Tenor d1, final Tenor d2, final Period tolerance) {
    if (tolerance.equals(TOLERANCE)) {
      return d1.compareTo(d2); //TOLERANCE == 1ns => this degenerate case
    }
    if (d1.equals(d2)) {
      return 0;
    }
    final Period dLow = d1.getPeriod().minus(tolerance);
    final Period deltaLow = d2.getPeriod().minus(dLow);
    if (deltaLow.isNegative()) {
      return -1;
    }
    final Period dHigh = d1.getPeriod().plus(tolerance);
    final Period deltaHigh = dHigh.minus(d2.getPeriod());
    if (deltaHigh.isNegative()) {
      return 1;
    }
    return 0;
  }

  @Override
  public LabelledMatrix1D<Tenor, Period> getMatrix(final Tenor[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    return new TenorLabelledMatrix1D(keys, labels, labelsTitle, values, valuesTitle);
  }

  @Override
  public LabelledMatrix1D<Tenor, Period> getMatrix(final Tenor[] keys, final Object[] labels, final double[] values) {
    return new TenorLabelledMatrix1D(keys, labels, values);
  }

  @Override
  public LabelledMatrix1D<Tenor, Period> getMatrix(final Tenor[] keys, final double[] values) {
    return new TenorLabelledMatrix1D(keys, values);
  }

}
