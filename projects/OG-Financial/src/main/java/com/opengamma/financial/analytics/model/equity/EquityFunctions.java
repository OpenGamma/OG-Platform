/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.equity.option.EquityVanillaBarrierOptionDistanceFunction;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.varianceswap.VarianceSwapFunctions;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class EquityFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new EquityFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(AffineDividendFunction.class));
    functions.add(functionConfiguration(EquityForwardCurveFunction.class));
    functions.add(functionConfiguration(EquityForwardCurveFromFutureCurveFunction.class));
    functions.add(functionConfiguration(SecurityMarketPriceFunction.class));
    functions.add(functionConfiguration(EquitySecurityDeltaFunction.class));
    functions.add(functionConfiguration(EquitySecurityValueDeltaFunction.class));
    functions.add(functionConfiguration(EquitySecurityScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionDistanceFunction.class));
    functions.add(functionConfiguration(EquityOptionMonetizedVegaFunction.class));
  }

  protected FunctionConfigurationSource futuresFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected FunctionConfigurationSource portfolioTheoryFunctionConfiguration() {
    return PortfolioTheoryFunctions.instance();
  }

  protected FunctionConfigurationSource varianceSwapFunctionConfiguration() {
    return VarianceSwapFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), futuresFunctionConfiguration(), optionFunctionConfiguration(), portfolioTheoryFunctionConfiguration(),
        varianceSwapFunctionConfiguration());
  }

}
