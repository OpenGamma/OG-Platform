/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.List;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 * Currently not referenced from StandardFunctionConfiguration or its children. 
 */
public class DividendYieldFuturesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a function configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new FutureFunctions().getObjectCreating();
  }
  
  private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
  private String _closingPriceField;
  private String _costOfCarryField = "COST_OF_CARRY";
  private String _valueFieldName = MarketDataRequirementNames.MARKET_VALUE;

  public void setHtsResolutionKey(final String htsResolutionKey) {
    _htsResolutionKey = htsResolutionKey;
  }

  public String getHtsResolutionKey() {
    return _htsResolutionKey;
  }

  public void setClosingPriceField(final String closingPriceField) {
    _closingPriceField = closingPriceField;
  }

  public String getClosingPriceField() {
    return _closingPriceField;
  }

  public void setCostOfCarryField(final String costOfCarryField) {
    _costOfCarryField = costOfCarryField;
  }

  public String getCostOfCarryField() {
    return _costOfCarryField;
  }

  public void setValueFieldName(final String valueFieldName) {
    _valueFieldName = valueFieldName;
  }

  public String getValueFieldName() {
    return _valueFieldName;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getHtsResolutionKey(), "htsResolutionKey");
    ArgumentChecker.notNullInjected(getClosingPriceField(), "closingPriceField");
    ArgumentChecker.notNullInjected(getCostOfCarryField(), "costOfCarryField");
    ArgumentChecker.notNullInjected(getValueFieldName(), "valueFieldName");
    super.afterPropertiesSet();
  }
  
  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityDividendYieldForwardFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldPresentValueFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldPV01FuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldSpotFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldValueDeltaFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldValueRhoFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    functions.add(functionConfiguration(EquityDividendYieldFuturesYCNSFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
    // TODO: add other package functions
  }

}
