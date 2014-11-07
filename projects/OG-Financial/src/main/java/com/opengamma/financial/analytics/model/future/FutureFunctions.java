/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;


/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new FutureFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfiguration;

      public void setCurveConfiguration(final String curveConfiguration) {
        _curveConfiguration = curveConfiguration;
      }

      public String getCurveConfiguration() {
        return _curveConfiguration;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();

    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

    protected void addInterestRateFutureDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 2];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(InterestRateFutureDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addInterestRateFutureDefaults(functions);
      }
      functions.add(functionConfiguration(InterestRateFuturePresentValueFunction.class));
      functions.add(functionConfiguration(InterestRateFuturePV01Function.class));
      functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunction.class));
    }
  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractFunctionConfigurationBean {

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
      functions.add(functionConfiguration(MarkToMarketForwardFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketPresentValueFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketPV01FuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketSpotFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketValueDeltaFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketValueRhoFuturesFunction.class, getClosingPriceField(), getCostOfCarryField(), getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketScenarioPnLFuturesFunction.class));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FutureSecurityDeltaFunction.class));
    functions.add(functionConfiguration(FutureSecurityValueDeltaFunction.class));
    // TODO: add functions from package
  }

}
