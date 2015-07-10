/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.marketdata.MulticurveMarketDataBuilder;
import com.opengamma.util.result.Result;


/**
 * Function implementation that for a particular curve config, determines which
 * curves need to go into a multicurve bundle and coordinates the building
 * of them. This ensures that a particular curve only needs to get built once.
 *
 * @deprecated curves are built using {@link MulticurveMarketDataBuilder}.
 */
@Deprecated
public interface DiscountingMulticurveBundleResolverFn {

  /**
   * Generates a multicurve bundle for the supplied curve config.
   *
   * @param env the environment
   * @param curveConfig the curve to build the multicurve bundle for
   * @return result containing the multicurve data, if successful
   */
  @Cacheable
  @Output(OutputNames.DISCOUNTING_MULTICURVE_BUNDLE)
  Result<MulticurveBundle> generateBundle(Environment env, CurveConstructionConfiguration curveConfig);

  /**
   * TODO what's this for? it's only used in one place. is ImpliedDepositCurveData a piece of market data?
   * Extracts the implied deposit curve data for the supplied curve config.
   *
   * @param env the environment
   * @param curveConfig the curve to extract the implied deposit curve data for
   * @return result containing the implied deposit curve data, if successful
   * @deprecated this doesn't belong here
   */
  @Deprecated
  @Cacheable
  Result<ImpliedDepositCurveData> extractImpliedDepositCurveData(
      Environment env, CurveConstructionConfiguration curveConfig);
}
