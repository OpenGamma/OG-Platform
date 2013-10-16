/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CurrencyFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new CurrencyFunctions().getObjectCreating();
  }

  protected void addDefaultCurrencyFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(DefaultCurrencyFunction.Permissive.class, requirementName));
  }

  public void addCurrencyConversionFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(CurrencyConversionFunction.class, requirementName));
    addDefaultCurrencyFunction(functions, requirementName);
  }

  public void addCurrencySeriesConversionFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(CurrencySeriesConversionFunction.class, requirementName));
    addDefaultCurrencyFunction(functions, requirementName);
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CurrencyMatrixSeriesSourcingFunction.class));
    functions.add(functionConfiguration(CurrencyMatrixSpotSourcingFunction.class));
    addCurrencyConversionFunction(functions, ValueRequirementNames.DAILY_PNL);
    addCurrencyConversionFunction(functions, ValueRequirementNames.DV01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.CS01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.GAMMA_CS01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.RR01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.IR01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.JUMP_TO_DEFAULT);
    addCurrencyConversionFunction(functions, ValueRequirementNames.FAIR_VALUE);
    addCurrencySeriesConversionFunction(functions, ValueRequirementNames.PNL_SERIES);
    addCurrencySeriesConversionFunction(functions, ValueRequirementNames.YIELD_CURVE_PNL_SERIES);
    addCurrencySeriesConversionFunction(functions, ValueRequirementNames.CURVE_PNL_SERIES);
    addCurrencyConversionFunction(functions, ValueRequirementNames.PRESENT_VALUE);
    //TODO PRESENT_VALUE_CURVE_SENSITIVITY
    addCurrencyConversionFunction(functions, ValueRequirementNames.GAMMA_PV01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.PV01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_DELTA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_GAMMA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_GAMMA_P);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_PHI);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_RHO);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_SPEED);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_THETA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_VANNA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_VEGA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.VALUE_VOMMA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addCurrencyConversionFunction(functions, ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES);
    addCurrencyConversionFunction(functions, ValueRequirementNames.MONETIZED_VEGA);
    addCurrencyConversionFunction(functions, ValueRequirementNames.BUCKETED_CS01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.BUCKETED_GAMMA_CS01);
    addCurrencyConversionFunction(functions, ValueRequirementNames.BUCKETED_IR01);
  }

}
