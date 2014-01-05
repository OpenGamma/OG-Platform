/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a provider with inflation (which contain multicurve) and Black inflation zero-coupon cap/floor parameters.
 */
public class BlackSmileCapInflationZeroCouponProvider implements BlackSmileCapInflationZeroCouponProviderInterface {

  /**
   * The inflation provider.
   */
  private final InflationProviderInterface _inflation;
  /**
   * The Black parameters.
   */
  private final BlackSmileCapInflationZeroCouponParameters _parameters;

  /**
   * Constructor.
   * @param inflation The inflation provider, not null
   * @param parameters The Black parameters, not null
   */
  public BlackSmileCapInflationZeroCouponProvider(final InflationProviderInterface inflation, final BlackSmileCapInflationZeroCouponParameters parameters) {
    ArgumentChecker.notNull(inflation, "inflation");
    ArgumentChecker.notNull(parameters, "parameters");
    _inflation = inflation;
    _parameters = parameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _inflation.getMulticurveProvider();
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    return _inflation;
  }

  @Override
  public BlackSmileCapInflationZeroCouponProviderInterface copy() {
    final InflationProviderInterface inflation = _inflation.copy();
    return new BlackSmileCapInflationZeroCouponProvider(inflation, _parameters);
  }

  @Override
  public BlackSmileCapInflationZeroCouponParameters getBlackParameters() {
    return _parameters;
  }

  @Override
  public double[] parameterInflationSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _inflation.parameterInflationSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _inflation.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _inflation.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _inflation.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _inflation.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSmileCapInflationZeroCouponProvider)) {
      return false;
    }
    final BlackSmileCapInflationZeroCouponProvider other = (BlackSmileCapInflationZeroCouponProvider) obj;
    if (!ObjectUtils.equals(_inflation, other._inflation)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
