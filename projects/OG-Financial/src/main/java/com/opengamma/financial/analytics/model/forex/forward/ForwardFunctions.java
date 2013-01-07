/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.DeprecatedFunctions;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForwardFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new ForwardFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource deprecated() {
    return DeprecatedFunctions.DEFAULT;
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardPV01Function.class));
    functions.add(functionConfiguration(FXForwardFXPresentValueFunction.class));
    functions.add(functionConfiguration(FXForwardCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXForwardYCNSFunction.class));
    functions.add(functionConfiguration(FXForwardPresentValueCurveSensitivityFunction.class));
  }

}
