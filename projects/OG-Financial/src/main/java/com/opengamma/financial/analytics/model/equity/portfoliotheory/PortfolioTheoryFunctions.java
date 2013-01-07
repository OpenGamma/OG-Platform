/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class PortfolioTheoryFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new PortfolioTheoryFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey) {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    functions.add(functionConfiguration(CAPMBetaModelPositionFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(CAPMBetaModelPortfolioNodeFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(CAPMFromRegressionModelFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(SharpeRatioPositionFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(SharpeRatioPortfolioNodeFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(TreynorRatioPositionFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(TreynorRatioPortfolioNodeFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(JensenAlphaFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(TotalRiskAlphaPositionFunction.class, htsResolutionKey));
    functions.add(functionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class, htsResolutionKey));
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(functions));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(StandardEquityModelFunction.class));
  }

}
