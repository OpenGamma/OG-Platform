/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class PNLFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new PNLFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource deprecated() {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    functions.add(functionConfiguration(YieldCurveNodePnLFunctionDeprecated.class));
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(functions));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityPnLFunction.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, MissingInputsFunction.AGGREGATION_STYLE_FULL));
    // TODO: Other functions from this package
  }
}
