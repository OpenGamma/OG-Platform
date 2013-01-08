/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.aggregation.AggregationFunctions;
import com.opengamma.financial.analytics.AnalyticsFunctions;
import com.opengamma.financial.currency.CurrencyFunctions;
import com.opengamma.financial.property.PropertyFunctions;
import com.opengamma.financial.value.ValueFunctions;
import com.opengamma.financial.view.ViewFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class FinancialFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new FinancialFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected RepositoryConfigurationSource aggregationFunctionConfiguration() {
    return AggregationFunctions.instance();
  }

  protected RepositoryConfigurationSource analyticsFunctionConfiguration() {
    return AnalyticsFunctions.instance();
  }

  protected RepositoryConfigurationSource currencyFunctionConfiguration() {
    return CurrencyFunctions.instance();
  }

  protected RepositoryConfigurationSource propertyFunctionConfiguration() {
    return PropertyFunctions.instance();
  }

  protected RepositoryConfigurationSource valueFunctionConfiguration() {
    return ValueFunctions.instance();
  }

  protected RepositoryConfigurationSource viewFunctionConfiguration() {
    return ViewFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), aggregationFunctionConfiguration(), analyticsFunctionConfiguration(), currencyFunctionConfiguration(),
        propertyFunctionConfiguration(), valueFunctionConfiguration(), viewFunctionConfiguration());
  }

}
