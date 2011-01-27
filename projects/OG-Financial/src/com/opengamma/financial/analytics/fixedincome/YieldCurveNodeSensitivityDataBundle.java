/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;

/**
 * 
 */
public class YieldCurveNodeSensitivityDataBundle {
  private final DoubleLabelledMatrix1D _labelledMatrix;
  private final Currency _currency;
  private final String _yieldCurveName;

  public YieldCurveNodeSensitivityDataBundle(final DoubleLabelledMatrix1D labelledMatrix, final Currency currency, final String yieldCurveName) {
    Validate.notNull(labelledMatrix, "labelled matrix");
    Validate.notNull(currency, "currency");
    Validate.notNull(yieldCurveName, "yield curve name");
    _labelledMatrix = labelledMatrix;
    _currency = currency;
    _yieldCurveName = yieldCurveName;
  }

  public DoubleLabelledMatrix1D getLabelledMatrix() {
    return _labelledMatrix;
  }

  public Currency getCurrency() {
    return _currency;
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
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldCurveName, other._yieldCurveName)) {
      return false;
    }
    return ObjectUtils.equals(_labelledMatrix, other._labelledMatrix);
  }

}
