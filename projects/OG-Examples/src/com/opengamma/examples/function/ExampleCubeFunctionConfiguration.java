/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.examples.volatility.cube.ExampleSwaptionVolatilityCubeInstrumentProvider;
import com.opengamma.examples.volatility.cube.ExampleVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunction;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.money.Currency;


/**
 * 
 */
public class ExampleCubeFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }
  
  public RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();
    addSwaptionVolCubeFunction(configs);
    return new RepositoryConfiguration(configs);
  }

  public RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  private void addSwaptionVolCubeFunction(final List<FunctionConfiguration> configs) {
    final Set<Currency> volCubeCurrencies = ExampleSwaptionVolatilityCubeInstrumentProvider.INSTANCE.getAllCurrencies();
    for (final Currency currency : volCubeCurrencies) {
      addVolatilityCubeFunction(configs, currency.getCode(), ExampleVolatilityCubeDefinitionSource.DEFINITION_NAME);
    }
  }

  private void addVolatilityCubeFunction(final List<FunctionConfiguration> configs, final String... parameters) {
    addVolatilityCubeFunction(configs, Arrays.asList(parameters));
  }

  private void addVolatilityCubeFunction(final List<FunctionConfiguration> configs, final List<String> parameters) {
    if (parameters.size() != 2) {
      throw new IllegalArgumentException();
    }
    configs.add(new ParameterizedFunctionConfiguration(VolatilityCubeFunction.class.getName(), parameters));
    configs.add(new ParameterizedFunctionConfiguration(SyntheticVolatilityCubeMarketDataFunction.class.getName(), parameters));
  }

}
