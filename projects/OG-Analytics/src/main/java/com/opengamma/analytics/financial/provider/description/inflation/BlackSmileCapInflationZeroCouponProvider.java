/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a provider with inflation (which contian multicurve) and Black inflation zero-coupon cap/floor parameters.
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
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapInflationZeroCouponProvider(final InflationProviderInterface inflation, final BlackSmileCapInflationZeroCouponParameters parameters) {
    ArgumentChecker.notNull(inflation, "Inflation provider");
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
    InflationProviderInterface inflation = _inflation.copy();
    return new BlackSmileCapInflationZeroCouponProvider(inflation, _parameters);
  }

  @Override
  public BlackSmileCapInflationZeroCouponParameters getBlackParameters() {
    return _parameters;
  }

}
