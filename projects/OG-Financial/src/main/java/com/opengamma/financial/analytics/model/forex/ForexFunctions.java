/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.forex.option.OptionFunctions;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class ForexFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new ForexFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ConventionBasedFXRateFunction.class));
    functions.add(functionConfiguration(SecurityFXHistoricalTimeSeriesFunction.class));
  }

  /**
   * Adds deprecated FX forward functions to the repository
   * @return The configuration source populated with FX forward functions.
   * @deprecated The functions that are added are deprecated
   */
  @Deprecated
  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), forwardFunctionConfiguration(), optionFunctionConfiguration());
  }

}
