/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;

/**
 * 
 */
public class ISDAYieldCurveAndHazardRateCurveProvider {
  private final ISDADateCurve _yieldCurve;
  private final HazardRateCurve _hazardRateCurve;

  public ISDAYieldCurveAndHazardRateCurveProvider(final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve) {
    _yieldCurve = yieldCurve;
    _hazardRateCurve = hazardRateCurve;
  }

  public ISDADateCurve getYieldCurve() {
    return _yieldCurve;
  }

  public HazardRateCurve getHazardRateCurve() {
    return _hazardRateCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_hazardRateCurve == null) ? 0 : _hazardRateCurve.hashCode());
    result = prime * result + ((_yieldCurve == null) ? 0 : _yieldCurve.hashCode());
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
    final ISDAYieldCurveAndHazardRateCurveProvider other = (ISDAYieldCurveAndHazardRateCurveProvider) obj;
    if (_hazardRateCurve == null) {
      if (other._hazardRateCurve != null) {
        return false;
      }
    } else if (!_hazardRateCurve.equals(other._hazardRateCurve)) {
      return false;
    }
    if (_yieldCurve == null) {
      if (other._yieldCurve != null) {
        return false;
      }
    } else if (!_yieldCurve.equals(other._yieldCurve)) {
      return false;
    }
    return true;
  }


}
