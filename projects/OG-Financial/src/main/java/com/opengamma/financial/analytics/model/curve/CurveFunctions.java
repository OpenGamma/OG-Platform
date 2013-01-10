/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.future.FutureFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class CurveFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected RepositoryConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected RepositoryConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected RepositoryConfigurationSource interestRateFunctionConfiguration() {
    return InterestRateFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), forwardFunctionConfiguration(), futureFunctionConfiguration(), interestRateFunctionConfiguration());
  }

}
