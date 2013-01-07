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
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class EquityFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new EquityFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(AffineDividendFunction.class));
    functions.add(functionConfiguration(EquityForwardCurveFunction.class));
    functions.add(functionConfiguration(SecurityMarketPriceFunction.class));
  }

  protected RepositoryConfigurationSource futuresFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctionConfiguration() {
    return PortfolioTheoryFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource varianceSwapFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), futuresFunctionConfiguration(), optionFunctionConfiguration(), portfolioTheoryFunctionConfiguration(),
        varianceSwapFunctionConfiguration());
  }

}
