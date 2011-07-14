/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.volatility.surface.EquityOptionVolatilitySurfaceDataFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates function repository configuration for surface supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the surfaces and currencies available. 
 */
public class DemoSurfaceFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  public static RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();

    configs.add(new ParameterizedFunctionConfiguration(RawVolatilitySurfaceDataFunction.class.getName(), Arrays.asList("DEFAULT", "SWAPTION", "DEFAULT")));
    configs.add(new ParameterizedFunctionConfiguration(RawVolatilitySurfaceDataFunction.class.getName(), Arrays.asList("DEFAULT", "IR_FUTURE", "DEFAULT")));
    configs.add(new ParameterizedFunctionConfiguration(RawVolatilitySurfaceDataFunction.class.getName(), Arrays.asList("DEFAULT", "FX_VANILLA_OPTION", "DEFAULT")));
    configs.add(new ParameterizedFunctionConfiguration(EquityOptionVolatilitySurfaceDataFunction.class.getName(), Arrays.asList("DEFAULT", "EQUITY_OPTION", "DEFAULT")));
    return new RepositoryConfiguration(configs);
  }

  public static RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new RepositoryConfigurationSource() {
      private final RepositoryConfiguration _config = constructRepositoryConfiguration();

      @Override
      public RepositoryConfiguration getRepositoryConfiguration() {
        return _config;
      }
    };
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
