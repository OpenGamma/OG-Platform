/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class PortfolioTheoryFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new PortfolioTheoryFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey) {
    final Calculators factory = new Calculators();
    factory.setHtsResolutionKey(htsResolutionKey);
    return factory.getObjectCreating();
  }

  /**
   * Function repository configuration source for the functions contained in this package.
   */
  public static class Calculators extends AbstractRepositoryConfigurationBean {

    private String _htsResolutionKey;

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(CAPMBetaModelPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(CAPMBetaModelPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(CAPMFromRegressionModelFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SharpeRatioPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SharpeRatioPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TreynorRatioPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TreynorRatioPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(JensenAlphaFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TotalRiskAlphaPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class, getHtsResolutionKey()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(StandardEquityModelFunction.class));
  }

}
