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
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateCurveFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class CurveFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new CurveFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected RepositoryConfigurationSource interestRateFunctionConfiguration() {
    return InterestRateCurveFunctions.DEFAULT;
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), interestRateFunctionConfiguration());
  }

}
