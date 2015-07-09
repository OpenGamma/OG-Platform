/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.curve;

import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.IsdaYieldCurveDataId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * A provider function which, given a yield key, will return a valid {@link YieldCurveData} instance
 * via the market data environment.
 */
public class DefaultYieldCurveDataProviderFn implements YieldCurveDataProviderFn {

  private final String _yieldCurveDataName;

  /**
   * Creates an instance.
   * @param yieldCurveDataName the name of the curve data snapshot to source curve data from
   */
  public DefaultYieldCurveDataProviderFn(String yieldCurveDataName) {
    _yieldCurveDataName = ArgumentChecker.notNull(yieldCurveDataName, "yieldCurveDataName");
  }

  @Override
  public Result<YieldCurveData> retrieveYieldCurveData(Environment env, Currency currency) {
    IsdaYieldCurveDataId yieldCurveDataId = IsdaYieldCurveDataId.of(_yieldCurveDataName, currency);
    return env.getMarketDataBundle().get(yieldCurveDataId, YieldCurveData.class);
  }

}
