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
  
  public TenorLabelledMatrix1D(final Tenor[] keys, final double[] values) {
    super(keys, values, LabelledMatrixUtils.TENOR_TOLERANCE);
  }

  public TenorLabelledMatrix1D(final Tenor[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, LabelledMatrixUtils.TENOR_TOLERANCE);
  }

  public TenorLabelledMatrix1D(final Tenor[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    super(keys, labels, labelsTitle, values, valuesTitle, LabelledMatrixUtils.TENOR_TOLERANCE);
  }

  @Override
  public int compare(final Tenor d1, final Tenor d2, final Period tolerance) {
    return LabelledMatrixUtils.compareTenorsWithTolerance(d1, d2, tolerance);
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
