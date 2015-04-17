/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.curve;

import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * A provider function which, given a currency, will return a valid {@link YieldCurveData} instance.
 */
public interface YieldCurveDataProviderFn {

  /**
   * Loads the {@link YieldCurveData} instance associated with the given currency.
   * @param env the execution environment
   * @param currency a currency
   * @return a valid {@link YieldCurveData} object or failure
   */
  @Cacheable
  Result<YieldCurveData> retrieveYieldCurveData(Environment env, Currency currency);

}
