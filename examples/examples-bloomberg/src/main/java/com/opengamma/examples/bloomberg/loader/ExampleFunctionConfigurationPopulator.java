/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.DOUBLE_QUADRATIC;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.aggregation.AggregationFunctions;
import com.opengamma.financial.analytics.AnalyticsFunctions;
import com.opengamma.financial.analytics.ircurve.IRCurveFunctions;
import com.opengamma.financial.analytics.model.ModelFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackSurfaceDefaults;
import com.opengamma.financial.currency.CurrencyFunctions;
import com.opengamma.financial.property.PropertyFunctions;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.financial.value.ValueFunctions;
import com.opengamma.financial.view.ViewFunctions;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.scripts.Scriptable;
import com.opengamma.web.spring.DemoStandardFunctionConfiguration;

/**
 * Example code to create the function configurations
 * <p>
 */
@Scriptable
public class ExampleFunctionConfigurationPopulator extends AbstractTool<ToolContext> {

  private static final String CURVE = "CURVE_FUNCTIONS";
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
    AnalyticsFunctions analyticsFunctions = new AnalyticsFunctions() {
      @Override
      protected FunctionConfigurationSource modelFunctionConfiguration() {
        ModelFunctions modelFunctions = new ModelFunctions() {
          @Override
          protected FunctionConfigurationSource equityFunctionConfiguration() {
            EquityFunctions equityFunctions = new EquityFunctions() {
              @Override
              protected FunctionConfigurationSource optionFunctionConfiguration() {
                return null;
              }
            };
            return equityFunctions.getObjectCreating();
          }
        };
        return modelFunctions.getObjectCreating();
      }
    };
    storeFunctionDefinition(AGGREGATION, AggregationFunctions.instance());
    storeFunctionDefinition(ANALYTICS, analyticsFunctions.getObjectCreating());
    storeFunctionDefinition(CURRENCY, CurrencyFunctions.instance());
    storeFunctionDefinition(PROPERTY, PropertyFunctions.instance());
    storeFunctionDefinition(VALUE, ValueFunctions.instance());
    storeFunctionDefinition(VIEW, ViewFunctions.instance());

    FunctionConfigurationDefinition financialFunc = new FunctionConfigurationDefinition(FINANCIAL,
        ImmutableList.of(AGGREGATION, ANALYTICS, CURRENCY, PROPERTY, VALUE, VIEW),
        Collections.<StaticFunctionConfiguration>emptyList(),
        Collections.<ParameterizedFunctionConfiguration>emptyList());
    storeFunctionDefinition(financialFunc);

    storeFunctionDefinition(STANDARD, DemoStandardFunctionConfiguration.instance());
    storeFunctionDefinition(CURVE, IRCurveFunctions.providers(getToolContext().getConfigMaster()));

    FunctionConfigurationDefinition exampleFunc = new FunctionConfigurationDefinition(EXAMPLE,
        ImmutableList.of(FINANCIAL, STANDARD, CURVE, CUBE),
        Collections.<StaticFunctionConfiguration>emptyList(),
        ImmutableList.of(new ParameterizedFunctionConfiguration(FXOptionBlackSurfaceDefaults.class.getName(),
            Arrays.asList(DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, LINEAR_EXTRAPOLATOR, "USD", "EUR", "DEFAULT"))));
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
