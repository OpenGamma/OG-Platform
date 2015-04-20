/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.cache.CacheLifetime;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.sesame.function.Output;
import com.opengamma.util.result.Result;

/**
 * Function capable of providing a discounting issuer provider curve bundle.
 */
public interface IssuerProviderBundleFn {

  @Cacheable(CacheLifetime.NEXT_FUTURE_ROLL)
  @Output(OutputNames.ISSUER_PROVIDER_BUNDLE)
  Result<IssuerProviderBundle> generateBundle(Environment env, CurveConstructionConfiguration curveConfig);

}
