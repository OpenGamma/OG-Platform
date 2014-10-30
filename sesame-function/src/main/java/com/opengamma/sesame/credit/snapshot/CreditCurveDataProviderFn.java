/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.snapshot;

import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.util.result.Result;

/**
 * A provider function which, given a credit key, will return a valid {@link CreditCurveData} instance.
 */
public interface CreditCurveDataProviderFn {

  
  /**
   * Retrieve the credit data for the given key.
   * @param env the execution environment
   * @param key a credit curve data key
   * @return a {@link CreditCurveData} result
   */
  @Cacheable
  Result<CreditCurveData> retrieveCreditCurveData(Environment env, CreditCurveDataKey key);
  
  
}
