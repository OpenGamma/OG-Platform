/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.curve;

import java.util.Map;

import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.CreditCurveDataId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * A provider function which, given a credit key, will return a valid {@link CreditCurveData} instance
 * via the market data environment.
 */
public class DefaultCreditCurveDataProviderFn implements CreditCurveDataProviderFn {

  private final String _creditCurveDataName;

  /**
   * Creates an instance.
   * @param creditCurveDataName the name of the curve data snapshot to source curve data from
   */
  public DefaultCreditCurveDataProviderFn(String creditCurveDataName) {
    _creditCurveDataName = ArgumentChecker.notNull(creditCurveDataName, "creditCurveDataName");
  }

  @Override
  public Result<CreditCurveData> retrieveCreditCurveData(Environment env, CreditCurveDataKey key) {

    CreditCurveDataId creditCurveDataId = CreditCurveDataId.of(_creditCurveDataName);
    Result<CreditCurveDataSnapshot> curveResult =
        env.getMarketDataBundle().get(creditCurveDataId, CreditCurveDataSnapshot.class);

    if (!curveResult.isSuccess()) {
      return Result.failure(curveResult);
    }
    CreditCurveDataSnapshot curveData = curveResult.getValue();

    Map<CreditCurveDataKey, CreditCurveData> creditCurveDataMap = curveData.getCreditCurves();
    if (creditCurveDataMap.containsKey(key)) {
      return Result.success(creditCurveDataMap.get(key));
    } else {
      return Result.failure(FailureStatus.MISSING_DATA,
                            "Failed to load curve data for credit curve key {} in snapshot {} for valuation {}",
                            key,
                            curveData.getName(),
                            env.getValuationDate());
    }
  }

}
