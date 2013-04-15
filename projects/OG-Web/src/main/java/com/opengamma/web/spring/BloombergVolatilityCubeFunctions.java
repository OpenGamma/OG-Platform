/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.List;
import java.util.Set;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.volatility.cube.BloombergSwaptionVolatilityCubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.BloombergVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunction;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeMarketDataFunction;
import com.opengamma.util.money.Currency;

/**
 * Function repository configuration source for the Bloomberg based volatility cubes.
 */
public class BloombergVolatilityCubeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BloombergVolatilityCubeFunctions().getObjectCreating();
  }

  protected void addVolatilityCubeFunction(final List<FunctionConfiguration> functions, final String currency, final String definition) {
    functions.add(functionConfiguration(VolatilityCubeFunction.class, currency, definition));
    functions.add(functionConfiguration(VolatilityCubeMarketDataFunction.class, currency, definition));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    final Set<Currency> volCubeCurrencies = BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG.getAllCurrencies();
    for (final Currency currency : volCubeCurrencies) {
      addVolatilityCubeFunction(functions, currency.getCode(), BloombergVolatilityCubeDefinitionSource.DEFINITION_NAME);
    }
  }

}
