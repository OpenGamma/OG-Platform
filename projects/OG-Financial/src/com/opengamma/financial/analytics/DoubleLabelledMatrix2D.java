/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

/**
 * 
 */
public class DoubleLabelledMatrix2D extends LabelledMatrix2D<Double, Double> {

  public DoubleLabelledMatrix2D(Double[] xKeys, Double[] yKeys, double[][] values) {
    super(xKeys, yKeys, values);
  }

  public DoubleLabelledMatrix2D(Double[] xKeys, Object[] xLabels, Double[] yKeys, Object[] yLabels, double[][] values) {
    super(xKeys, xLabels, yKeys, yLabels, values);
  }
}
