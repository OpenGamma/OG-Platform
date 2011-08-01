/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

/**
 * 
 */
public class StringLabelledMatrix1D extends LabelledMatrix1D<String, String> {

  public StringLabelledMatrix1D(final String[] keys, final double[] values) {
    super(keys, values, null);
  }

  @Override
  public int compare(final String key1, final String key2, final String tolerance) {
    return key1.compareTo(key2);
  }

  @Override
  protected LabelledMatrix1D<String, String> getMatrix(final String[] keys, final Object[] labels, final double[] values) {
    return new StringLabelledMatrix1D(keys, values);
  }

  @Override
  protected LabelledMatrix1D<String, String> getMatrix(final String[] keys, final double[] values) {
    return new StringLabelledMatrix1D(keys, values);
  }

  @Override
  public LabelledMatrix1D<String, String> add(final LabelledMatrix1D<String, String> other) {
    return addIgnoringLabel(other);
  }

  @Override
  public LabelledMatrix1D<String, String> add(final LabelledMatrix1D<String, String> other, final String tolerance) {
    return addIgnoringLabel(other);
  }

}
