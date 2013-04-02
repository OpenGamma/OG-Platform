/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class VolatilityFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new VolatilityFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(VolatilitySurfaceSpecificationFunction.class));
    functions.add(functionConfiguration(VolatilitySurfaceDefinitionFunction.class));
  }

  protected FunctionConfigurationSource cubeFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  protected FunctionConfigurationSource fittedResultsFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  protected FunctionConfigurationSource surfaceFunctionConfiguration() {
    return SurfaceFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), cubeFunctionConfiguration(), fittedResultsFunctionConfiguration(), surfaceFunctionConfiguration());
  }

}
