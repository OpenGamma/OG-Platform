/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SwapStrip {
  private final Period _startTenor;
  private final Period _maturityTenor;
  private final String _payLegConventionName;
  private final String _receiveLegConventionName;
  private final String _curveSpecificationName;

  /**
   * 
   */
  public SwapStrip(final Period startTenor, final Period maturityTenor, final String payLegConventionName, final String receiveLegConventionName,
      final String curveSpecificationName) {
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(payLegConventionName, "pay leg convention name");
    ArgumentChecker.notNull(receiveLegConventionName, "receive leg convention name");
    ArgumentChecker.notNull(curveSpecificationName, "curve specification name");
    _startTenor = startTenor;
    _maturityTenor = maturityTenor;
    _payLegConventionName = payLegConventionName;
    _receiveLegConventionName = receiveLegConventionName;
    _curveSpecificationName = curveSpecificationName;
  }

  public Period getStartTenor() {
    return _startTenor;
  }

  public Period getMaturityTenor() {
    return _maturityTenor;
  }

  public String getPayLegConventionName() {
    return _payLegConventionName;
  }

  public String getReceiveLegConventionName() {
    return _receiveLegConventionName;
  }

  public String getCurveSpecificationName() {
    return _curveSpecificationName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveSpecificationName.hashCode();
    result = prime * result + _maturityTenor.hashCode();
    result = prime * result + _payLegConventionName.hashCode();
    result = prime * result + _receiveLegConventionName.hashCode();
    result = prime * result + _startTenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SwapStrip)) {
      return false;
    }
    final SwapStrip other = (SwapStrip) obj;
    if (!ObjectUtils.equals(_maturityTenor, other._maturityTenor)) {
      return false;
    }
    if (!ObjectUtils.equals(_payLegConventionName, other._payLegConventionName)) {
      return false;
    }
    if (!ObjectUtils.equals(_receiveLegConventionName, other._receiveLegConventionName)) {
      return false;
    }
    if (!ObjectUtils.equals(_startTenor, other._startTenor)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveSpecificationName, other._curveSpecificationName)) {
      return false;
    }
    return true;
  }



}
