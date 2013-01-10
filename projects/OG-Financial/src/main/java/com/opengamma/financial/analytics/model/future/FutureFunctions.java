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

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.future.FutureFunctions.Defaults.CurrencyInfo;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new FutureFunctions().getObjectCreating();
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
      functions.add(functionConfiguration(InterestRateFuturePresentValueFunctionDeprecated.class));
      functions.add(functionConfiguration(InterestRateFuturePV01FunctionDeprecated.class));
      functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunctionDeprecated.class));
    }

  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo) {
    final Defaults factory = new Defaults();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfiguration;

      public CurrencyInfo() {
      }

      public CurrencyInfo(final String curveConfiguration) {
        setCurveConfiguration(curveConfiguration);
      }

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

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();

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
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
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
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureGrossBasisFromCurvesFunction.class));
    functions.add(functionConfiguration(BondFutureNetBasisFromCurvesFunction.class));
    functions.add(functionConfiguration(InterestRateFuturePresentValueFunction.class));
    functions.add(functionConfiguration(InterestRateFuturePV01Function.class));
    functions.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(MarkToMarketForwardFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketPresentValueFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketPV01FuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketSpotFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketValueDeltaFuturesFunction.class));
    functions.add(functionConfiguration(MarkToMarketValueRhoFuturesFunction.class));
    // TODO: add functions from package
  }

}
