/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Data bundle with one equity price and a multi-curve provider.
 */
public class EquityTrsDataBundle implements ParameterProviderInterface {

  /** The equity price **/
  private final double _spotEquity;
  // TODO: Should this be replace by a map of LegalEntity/price (to be able to handle several equities in the same object).
  /** The multi-curve provider */
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

  @Override
  public ParameterProviderInterface copy() {
    final MulticurveProviderInterface multicurveProvider = _curves.copy();
    return new EquityTrsDataBundle(_spotEquity, multicurveProvider);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _curves.getMulticurveProvider();
  }

  @Override
  public double[] parameterSensitivity(String name, List<DoublesPair> pointSensitivity) {
    return _curves.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(String name, List<ForwardSensitivity> pointSensitivity) {
    return _curves.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _curves.getAllCurveNames();
  }
}
