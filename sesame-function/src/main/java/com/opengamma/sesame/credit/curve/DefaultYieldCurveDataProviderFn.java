/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.curve;

import java.util.Map;

import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.YieldCurveDataId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
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

    YieldCurveDataId yieldCurveDataId = YieldCurveDataId.of(_yieldCurveDataName);
    Result<YieldCurveDataSnapshot> curveResult =
        env.getMarketDataBundle().get(yieldCurveDataId, YieldCurveDataSnapshot.class);

    if (!curveResult.isSuccess()) {
      return Result.failure(curveResult);
    }
    YieldCurveDataSnapshot curveData = curveResult.getValue();

    Map<Currency, YieldCurveData> creditCurveDataMap = curveData.getYieldCurves();
    if (creditCurveDataMap.containsKey(currency)) {
      return Result.success(creditCurveDataMap.get(currency));
    } else {
      return Result.failure(FailureStatus.MISSING_DATA,
                            "Failed to load curve data for credit curve key {} in snapshot {}",
                            currency,
                            curveData.getName());
    }
  }

}
