/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class VarianceSwapFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new VarianceSwapFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityForwardFromSpotAndYieldCurveFunction.class));
    functions.add(functionConfiguration(EquityVarianceSwapPureImpliedVolPVFunction.class));
    functions.add(functionConfiguration(EquityVarianceSwapPureLocalVolPVFunction.class));
    functions.add(functionConfiguration(EquityVarianceSwapStaticReplicationPresentValueFunction.class));
    functions.add(functionConfiguration(EquityVarianceSwapStaticReplicationVegaFunction.class));
    functions.add(functionConfiguration(EquityVarianceSwapStaticReplicationYCNSFunction.class));
  }

}
