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
  
  public StringLabelledMatrix1D(final String[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, null);
  }
  
  public StringLabelledMatrix1D(final String[] keys, final String labelsTitle, final double[] values, final String valuesTitle) {
    super(keys, labelsTitle, values, valuesTitle, null);
  }

  public StringLabelledMatrix1D(final String[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    super(keys, labels, labelsTitle, values, valuesTitle, null);
  }

  @Override
  public int compare(final String key1, final String key2, final String tolerance) {
    return key1.compareTo(key2);
  }

  @Override
  public LabelledMatrix1D<String, String> getMatrix(final String[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    return new StringLabelledMatrix1D(keys, labels, labelsTitle, values, valuesTitle);
  }
  
  @Override
  public LabelledMatrix1D<String, String> getMatrix(final String[] keys, final Object[] labels, final double[] values) {
    return new StringLabelledMatrix1D(keys, labels, values);
  }

  @Override
  public LabelledMatrix1D<String, String> getMatrix(final String[] keys, final double[] values) {
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
