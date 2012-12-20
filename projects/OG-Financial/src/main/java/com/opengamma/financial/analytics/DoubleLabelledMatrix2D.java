/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class DoubleLabelledMatrix2D extends LabelledMatrix2D<Double, Double> {

  public DoubleLabelledMatrix2D(final Double[] xKeys, final Double[] yKeys, final double[][] values) {
    super(xKeys, yKeys, values);
  }

  public DoubleLabelledMatrix2D(final Double[] xKeys, final Object[] xLabels, final Double[] yKeys, final Object[] yLabels, final double[][] values) {
    super(xKeys, xLabels, yKeys, yLabels, values);
  }
  
  public DoubleLabelledMatrix2D(final Double[] xKeys, final Object[] xLabels, final String xTitle,
      final Double[] yKeys, final Object[] yLabels, final String yTitle, final double[][] values, final String valuesTitle) {
    super(xKeys, xLabels, xTitle, yKeys, yLabels, yTitle, values, valuesTitle);
  }
  
  @Override
  public LabelledMatrix2D<Double, Double> getMatrix(Double[] xKeys, Object[] xLabels, String xTitle, Double[] yKeys, Object[] yLabels, String yTitle, double[][] values, String valuesTitle) {
    return new DoubleLabelledMatrix2D(xKeys, xLabels, xTitle, yKeys, yLabels, yTitle, values, valuesTitle);
  }

  @Override
  public LabelledMatrix2D<Double, Double> getMatrix(final Double[] xKeys, final Object[] xLabels, final Double[] yKeys, final Object[] yLabels, final double[][] values) {
    return new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, values);
  }

  //TODO this isn't right in its treatment of generics
  @Override
  public <X> int compareX(final Double d1, final Double d2, final X tolerance) {
    try {
      final double tol = (Double) tolerance;
      return CompareUtils.compareWithTolerance(d1, d2, tol);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException(e.getMessage());
    }
  }

  @Override
  public <Y> int compareY(final Double d1, final Double d2, final Y tolerance) {
    try {
      final double tol = (Double) tolerance;
      return CompareUtils.compareWithTolerance(d1, d2, tol);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException(e.getMessage());
    }
  }
}
