/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a provider with inflation (which contain multicurve) and  inflation parameters needed for the convexity adjustemnts.
 */
public class InflationConvexityAdjustmentProvider implements InflationConvexityAdjustmentProviderInterface {

  /**
   * The inflation provider.
   */
  private final InflationProviderInterface _inflation;
  /**
   * The inflation convexity adjustment parameters.
   */
  private final InflationConvexityAdjustmentParameters _inflationConvexityAdjustmentsParameters;

  /**
   * The  Black volatility surface used in cap/floor ibor modeling.
   */
  private final BlackFlatCapFloorParameters _blackSmileIborCapParameters;

  /**
   * Constructor.
   * @param inflation The inflation provider.
   * @param inflationConvexityAdjustmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public InflationConvexityAdjustmentProvider(final InflationProviderInterface inflation, final InflationConvexityAdjustmentParameters inflationConvexityAdjustmentsParameters,
      final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    ArgumentChecker.notNull(inflation, "inflation");
    ArgumentChecker.notNull(inflationConvexityAdjustmentsParameters, "inflationConvexityAdjustmentsParameters");
    ArgumentChecker.notNull(blackSmileIborCapParameters, "blackSmileIborCapParameters");
    _inflation = inflation;
    _inflationConvexityAdjustmentsParameters = inflationConvexityAdjustmentsParameters;
    _blackSmileIborCapParameters = blackSmileIborCapParameters;
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
  public InflationConvexityAdjustmentProviderInterface copy() {
    final InflationProviderInterface inflation = _inflation.copy();
    return new InflationConvexityAdjustmentProvider(inflation, _inflationConvexityAdjustmentsParameters, _blackSmileIborCapParameters);
  }

  @Override
  public InflationConvexityAdjustmentParameters getInflationConvexityAdjustmentParameters() {
    return _inflationConvexityAdjustmentsParameters;
  }

  @Override
  public BlackFlatCapFloorParameters getBlackSmileIborCapParameters() {
    return _blackSmileIborCapParameters;
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
    result = prime * result + _blackSmileIborCapParameters.hashCode();
    result = prime * result + _inflation.hashCode();
    result = prime * result + _inflationConvexityAdjustmentsParameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InflationConvexityAdjustmentProvider)) {
      return false;
    }
    final InflationConvexityAdjustmentProvider other = (InflationConvexityAdjustmentProvider) obj;
    if (!ObjectUtils.equals(_inflation, other._inflation)) {
      return false;
    }
    if (!ObjectUtils.equals(_inflationConvexityAdjustmentsParameters, other._inflationConvexityAdjustmentsParameters)) {
      return false;
    }
    if (!ObjectUtils.equals(_blackSmileIborCapParameters, other._blackSmileIborCapParameters)) {
      return false;
    }
    return true;
  }

}
