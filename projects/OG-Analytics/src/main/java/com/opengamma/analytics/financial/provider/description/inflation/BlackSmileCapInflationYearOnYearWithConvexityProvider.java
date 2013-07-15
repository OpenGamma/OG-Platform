/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackFlatCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.model.option.parameters.InflationConvexityAdjustmentParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BlackSmileCapInflationYearOnYearWithConvexityProvider implements BlackSmileCapInflationYearOnYearWithConvexityProviderInterface {

  /**
   * The inflation provider.
   */
  private final InflationProviderInterface _inflation;
  /**
   * The Black parameters.
   */
  private final BlackSmileCapInflationYearOnYearParameters _parameters;
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
   * @param parameters The Black parameters.
   * @param inflationConvexityAdjutmentsParameters The inflation convexity adjustment parameters.
   * @param blackSmileIborCapParameters The Black volatility cap/floor (ibor)  parameters.
   */
  public BlackSmileCapInflationYearOnYearWithConvexityProvider(final InflationProviderInterface inflation, final BlackSmileCapInflationYearOnYearParameters parameters,
      final InflationConvexityAdjustmentParameters inflationConvexityAdjutmentsParameters, final BlackFlatCapFloorParameters blackSmileIborCapParameters) {
    ArgumentChecker.notNull(inflation, "Inflation provider");
    _inflation = inflation;
    _parameters = parameters;
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
  public BlackSmileCapInflationYearOnYearWithConvexityProvider copy() {
    InflationProviderInterface inflation = _inflation.copy();
    return new BlackSmileCapInflationYearOnYearWithConvexityProvider(inflation, _parameters, _inflationConvexityAdjutmentsParameters, _blackSmileIborCapParameters);
  }

  @Override
  public BlackSmileCapInflationYearOnYearParameters getBlackParameters() {
    return _parameters;
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
