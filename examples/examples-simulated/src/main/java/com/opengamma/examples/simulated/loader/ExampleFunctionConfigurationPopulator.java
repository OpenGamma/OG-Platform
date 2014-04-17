/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.examples.simulated.function.ExampleStandardFunctionConfiguration;
import com.opengamma.examples.simulated.function.SyntheticVolatilityCubeFunctions;
import com.opengamma.financial.aggregation.AggregationFunctions;
import com.opengamma.financial.analytics.AnalyticsFunctions;
import com.opengamma.financial.currency.CurrencyFunctions;
import com.opengamma.financial.property.PropertyFunctions;
import com.opengamma.financial.target.TargetFunctions;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.financial.value.ValueFunctions;
import com.opengamma.financial.view.ViewFunctions;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.scripts.Scriptable;

/**
 * Example code to create the function configurations
 * <p>
 */
@Scriptable
public class ExampleFunctionConfigurationPopulator extends AbstractTool<ToolContext> {

  private static final String STANDARD = "STANDARD_FUNCTIONS";
  private static final String VIEW = "VIEW_FUNCTIONS";
  private static final String VALUE = "VALUE_FUNCTIONS";
  private static final String PROPERTY = "PROPERTY_FUNCTIONS";
  private static final String CURRENCY = "CURRENCY_FUNCTIONS";
  private static final String ANALYTICS = "ANALYTICS_FUNCTIONS";
  private static final String AGGREGATION = "AGGREGATION_FUNCTIONS";
  private static final String FINANCIAL = "FINANCIAL_FUNCTIONS";
  private static final String EXAMPLE = "EXAMPLE_FUNCTIONS";
  private static final String CUBE = "CUBE_FUNCTIONS";
  private static final String TARGET = "TARGET_FUNCTIONS";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleFunctionConfigurationPopulator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeFunctionDefinition(AGGREGATION, AggregationFunctions.instance());
    storeFunctionDefinition(ANALYTICS, AnalyticsFunctions.instance());
    storeFunctionDefinition(CURRENCY, CurrencyFunctions.instance());
    storeFunctionDefinition(PROPERTY, PropertyFunctions.instance());
    storeFunctionDefinition(VALUE, ValueFunctions.instance());
    storeFunctionDefinition(VIEW, ViewFunctions.instance());
    storeFunctionDefinition(TARGET, TargetFunctions.instance());

    FunctionConfigurationDefinition financialFunc = new FunctionConfigurationDefinition(FINANCIAL,
        ImmutableList.of(AGGREGATION, ANALYTICS, CURRENCY, PROPERTY, TARGET, VALUE, VIEW),
        Collections.<StaticFunctionConfiguration>emptyList(),
        Collections.<ParameterizedFunctionConfiguration>emptyList());
    storeFunctionDefinition(financialFunc);

    storeFunctionDefinition(STANDARD, ExampleStandardFunctionConfiguration.instance());
    storeFunctionDefinition(CUBE, SyntheticVolatilityCubeFunctions.instance());

    FunctionConfigurationDefinition exampleFunc = new FunctionConfigurationDefinition(EXAMPLE,
        ImmutableList.of(FINANCIAL, STANDARD, CUBE),
        Collections.<StaticFunctionConfiguration>emptyList(),
        Collections.<ParameterizedFunctionConfiguration>emptyList());
    storeFunctionDefinition(exampleFunc);

  }

  private void storeFunctionDefinition(final FunctionConfigurationDefinition definition) {
    final ConfigItem<FunctionConfigurationDefinition> config = ConfigItem.of(definition, definition.getName(), FunctionConfigurationDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

  private void storeFunctionDefinition(final String name, final FunctionConfigurationSource funcConfigSource) {
    FunctionConfigurationDefinition definition = FunctionConfigurationDefinition.of(name, funcConfigSource);
    final ConfigItem<FunctionConfigurationDefinition> config = ConfigItem.of(definition, name, FunctionConfigurationDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

}
