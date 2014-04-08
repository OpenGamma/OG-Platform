/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityTrsDataBundle {
  private final double _spotEquity;
  private final MulticurveProviderInterface _curves;

  public EquityTrsDataBundle(final double spotEquity, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(curves, "curves");
    _spotEquity = spotEquity;
    _curves = curves;
  }

  /**
   * Gets the spot equity price.
   * @return the spot equity price
   */
  public double getSpotEquity() {
    return _spotEquity;
  }

  /**
   * Gets the curves.
   * @return the curves
   */
  public MulticurveProviderInterface getCurves() {
    return _curves;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curves.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spotEquity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof EquityTrsDataBundle)) {
      return false;
    }
    final EquityTrsDataBundle other = (EquityTrsDataBundle) obj;
    if (Double.compare(_spotEquity, other._spotEquity) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_curves, other._curves)) {
      return false;
    }
    return true;
  }


}
