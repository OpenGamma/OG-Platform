/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.function;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.<p>
 * The original intention was to be a group of functions that simply expose attributes of the security.
 */
public class SecurityFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new SecurityFunctions().getObjectCreating();
  }
  
  @Override
  protected void addAllConfigurations(List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ISINFunction.class));
    functions.add(functionConfiguration(BloombergBuidFunction.class));
    functions.add(functionConfiguration(BloombergTickerFunction.class));
  }
}
