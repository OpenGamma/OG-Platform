/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.List;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class PNLFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new PNLFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractRepositoryConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(YieldCurveNodePnLFunctionDeprecated.class));
    }

  }

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey, final String mark2marketField, final String costOfCarryField, final String valueFieldName) {
    final Calculators factory = new Calculators();
    factory.setHtsResolutionKey(htsResolutionKey);
    factory.setMark2MarketField(mark2marketField);
    factory.setCostOfCarryField(costOfCarryField);
    factory.setValueFieldName(valueFieldName);
    return factory.getObjectCreating();
  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractRepositoryConfigurationBean {

    private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;;
    private String _mark2MarketField;
    private String _costOfCarryField;
    private String _valueFieldName = MarketDataRequirementNames.MARKET_VALUE;

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    public void setMark2MarketField(final String mark2MarketField) {
      _mark2MarketField = mark2MarketField;
    }

    public String getMark2MarketField() {
      return _mark2MarketField;
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
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(TradeExchangeTradedPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(TradeExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(PositionExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(SecurityPriceSeriesFunction.class, getHtsResolutionKey(), getValueFieldName()));
      functions.add(functionConfiguration(SimpleFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SimpleFXFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(ValueGreekSensitivityPnLFunction.class, getHtsResolutionKey()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityPnLFunction.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, MissingInputsFunction.AGGREGATION_STYLE_FULL));
  }

}
