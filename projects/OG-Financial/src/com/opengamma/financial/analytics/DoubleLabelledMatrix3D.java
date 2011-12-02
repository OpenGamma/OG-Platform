/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class DoubleLabelledMatrix3D extends LabelledMatrix3D<Double, Double, Double, Double, Double, Double, DoubleLabelledMatrix3D> {

  public DoubleLabelledMatrix3D(final Double[] xKeys, final Double[] yKeys, final Double[] zKeys, final double[][][] values) {
    super(xKeys, yKeys, zKeys, values);
  }

  public DoubleLabelledMatrix3D(final Double[] xKeys, final Object[] xLabels, final Double[] yKeys, final Object[] yLabels, final Double[] zKeys, final Object[] zLabels, final double[][][] values) {
    super(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }

  @Override
  protected DoubleLabelledMatrix3D create(final Double[] xKeys, final Object[] xLabels, final Double[] yKeys, final Object[] yLabels, final Double[] zKeys, final Object[] zLabels,
      final double[][][] values) {
    return new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }

  public DoubleLabelledMatrix3D getMatrix(Double[] xKeys, Object[] xLabels, Double[] yKeys, Object[] yLabels, Double[] zKeys, Object[] zLabels, double[][][] values) {
    return new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }

  public DoubleLabelledMatrix3D getMatrix(final Double[] xKeys, final Double[] yKeys, final Double[] zKeys, final double[][][] values) {
    return new DoubleLabelledMatrix3D(xKeys, yKeys, zKeys, values);
  }
  
  @Override
  public Double getDefaultToleranceX() {
    return Double.MIN_NORMAL;
  }

  @Override
  public Double getDefaultToleranceY() {
    return Double.MIN_NORMAL;
  }

  @Override
  public Double getDefaultToleranceZ() {
    return Double.MIN_NORMAL;
  }

  @Override
  protected int compareKeysX(final Double key1, final Double key2, final Double tolerence) {
    return CompareUtils.compareWithTolerance(key1, key2, tolerence);
  }

  @Override
  protected int compareKeysY(final Double key1, final Double key2, final Double tolerence) {
    return CompareUtils.compareWithTolerance(key1, key2, tolerence);
  }

  @Override
  protected int compareKeysZ(final Double key1, final Double key2, final Double tolerence) {
    return CompareUtils.compareWithTolerance(key1, key2, tolerence);
  }

}
