/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.cube.CubeFunctions;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class VolatilityFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new VolatilityFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // No functions, just sub-packages
  }

  protected RepositoryConfigurationSource cubeFunctionConfiguration() {
    return CubeFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource localFunctionConfiguration() {
    return LocalFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource surfaceFunctionConfiguration() {
    return SurfaceFunctions.DEFAULT;
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), cubeFunctionConfiguration(), localFunctionConfiguration(), surfaceFunctionConfiguration());
  }

}
