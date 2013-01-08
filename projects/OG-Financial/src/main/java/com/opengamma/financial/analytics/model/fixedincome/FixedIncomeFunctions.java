/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.DeprecatedFunctions;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FixedIncomeFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new FixedIncomeFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentPresentValueFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentPV01Function.class));
    functions.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class));
  }

}
