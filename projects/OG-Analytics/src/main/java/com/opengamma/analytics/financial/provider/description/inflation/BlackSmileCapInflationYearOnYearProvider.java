/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 *  Implementation of a provider of Black smile for year on year inflation options. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying.
 */
public class BlackSmileCapInflationYearOnYearProvider implements BlackSmileCapInflationYearOnYearProviderInterface {

  /**
   * The inflation provider.
   */
  private final InflationProviderInterface _inflation;
  /**
   * The Black parameters.
   */
  private final BlackSmileCapInflationYearOnYearParameters _parameters;

  /**
   * Constructor.
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapInflationYearOnYearProvider(final InflationProviderInterface inflation, final BlackSmileCapInflationYearOnYearParameters parameters) {
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
  public BlackSmileCapInflationYearOnYearProviderInterface copy() {
    InflationProviderInterface inflation = _inflation.copy();
    return new BlackSmileCapInflationYearOnYearProvider(inflation, _parameters);
  }

  @Override
  public BlackSmileCapInflationYearOnYearParameters getBlackParameters() {
    return _parameters;
  }

}
