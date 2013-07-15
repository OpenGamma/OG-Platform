/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a provider with inflation (which contain multicurve) and  inflation parameters needed for the convexity adjustemnts.
 */
public class InflationConvexityAdjustmentProvider implements InflationConvexityAdjustmentProviderInterface {

  /**
   * The inflation provider.
   */
  private final InflationProviderInterface _inflation;
  /**
   * The inflation convextity adjustment parameters.
   */
  private final InflationConvexityAdjustmentParameters _inflationConvexityAdjutmentsParameters;

  /**
   * The  Black volatility surface used in cap/floor ibor modeling.
   */
  private final BlackFlatCapFloorParameters _blackSmileIborCapParameters;

  /**
   * Constructor.
   * @param inflation The inflation provider.
   * @param inflationConvexityAdjutmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public InflationConvexityAdjustmentProvider(final InflationProviderInterface inflation, final InflationConvexityAdjustmentParameters inflationConvexityAdjutmentsParameters,
      final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    ArgumentChecker.notNull(inflation, "Inflation provider");
    _inflation = inflation;
    _inflationConvexityAdjutmentsParameters = inflationConvexityAdjutmentsParameters;
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
    InflationProviderInterface inflation = _inflation.copy();
    return new InflationConvexityAdjustmentProvider(inflation, _inflationConvexityAdjutmentsParameters, _blackSmileIborCapParameters);
  }

  @Override
  public InflationConvexityAdjustmentParameters getInflationConvexityAdjustmentParameters() {
    return _inflationConvexityAdjutmentsParameters;
  }

  @Override
  public BlackFlatCapFloorParameters getBlackSmileIborCapParameters() {
    return _blackSmileIborCapParameters;
  }

}
