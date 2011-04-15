/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
//TODO is this needed?
public class YieldCurveNodeSensitivityDataBundle {
  private final DoubleLabelledMatrix1D _labelledMatrix;
  private final Currency _currency;
  private final String _yieldCurveName;

  public YieldCurveNodeSensitivityDataBundle(final Currency currency, final DoubleLabelledMatrix1D labelledMatrix, final String yieldCurveName) {
    Validate.notNull(labelledMatrix, "labelled matrix array");
    Validate.notNull(currency, "currency");
    Validate.notNull(yieldCurveName, "yield curve name array");
    _labelledMatrix = labelledMatrix;
    _currency = currency;
    _yieldCurveName = yieldCurveName;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public DoubleLabelledMatrix1D getLabelledMatrix() {
    return _labelledMatrix;
  }

  public String getYieldCurveName() {
    return _yieldCurveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _labelledMatrix.hashCode();
    result = prime * result + _yieldCurveName.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveNodeSensitivityDataBundle other = (YieldCurveNodeSensitivityDataBundle) obj;
    if (!ObjectUtils.equals(_labelledMatrix, other._labelledMatrix)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldCurveName, other._yieldCurveName)) {
      return false;
    }
    return ObjectUtils.equals(_currency, other._currency);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("\t" + _currency.getCode() + ", " + _yieldCurveName + "\n");
    final Object[] labels = _labelledMatrix.getLabels();
    final double[] values = _labelledMatrix.getValues();
    final int n = labels.length;
    for (int i = 0; i < n; i++) {
      sb.append(labels[i] + "\t" + values[i] + "\n");
    }
    return sb.toString();
  }
}
