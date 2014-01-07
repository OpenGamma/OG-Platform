/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated This class adds deprecated functions to the repository.
 */
@Deprecated
public class BasicBlackFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BasicBlackFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {
      /** The curve calculation configuration */
      private String _curveConfig;

      /**
       * Sets the curve calculation configuration.
       * @param curveConfig The curve calculation configuration
       */
      public void setCurveConfig(final String curveConfig) {
        _curveConfig = curveConfig;
      }

      /**
       * Gets the curve calculation configuration.
       * @return The curve calculation configuration.
       */
      public String getCurveConfig() {
        return _curveConfig;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfig(), "curveConfig");
      }

    }

    /** Map containing per-currency defaults */
    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    /** The number of days to use for horizon calculations */
    private int _numberOfDays = 1;

    /**
     * Sets the per-currency defaults
     * @param perCurrencyInfo The per-currency defaults
     */
    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    /**
     * Gets the per-currency defaults.
     * @return The per-currency defaults
     */
    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    /**
     * Adds an entry to the per-currency defaults.
     * @param currency The currency
     * @param info The defaults for this currency
     */
    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    /**
     * Gets the defaults for a currency.
     * @param currency The currency
     * @return The defaults for this currency
     */
    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

    /**
     * Sets the number of days to use in horizon calculations.
     * @param numberOfDays The number of days
     */
    public void setNumberOfDays(final int numberOfDays) {
      _numberOfDays = numberOfDays;
    }

    /**
     * Gets the number of days to use in horizon calculations.
     * @return The number of days
     */
    public int getNumberOfDays() {
      return _numberOfDays;
    }

    /**
     * Adds default functions for basic Black calculations to the function repository.
     * @param functions The functions
     */
    protected void addSwaptionBasicBlackDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 2];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfig();
      }
      functions.add(functionConfiguration(SwaptionBasicBlackDefaultPropertiesFunction.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addSwaptionBasicBlackDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(SwaptionBasicBlackPresentValueFunction.class));
    functions.add(functionConfiguration(SwaptionBasicBlackVolatilitySensitivityFunction.class));
    functions.add(functionConfiguration(SwaptionBasicBlackPV01Function.class));
    functions.add(functionConfiguration(SwaptionBasicBlackYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(SwaptionBasicBlackImpliedVolatilityFunction.class));
  }

}
