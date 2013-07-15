/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLFunction;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class SensitivitiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new SensitivitiesFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractFunctionConfigurationBean {

    private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNull(getHtsResolutionKey(), "htsResolutionKey");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(ExternallyProvidedSensitivityPnLFunction.class, getHtsResolutionKey()));
    }

  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

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

    protected void addExternallyProvidedSensitivitiesDefaultProperties(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 2];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(ExternallyProvidedSensitivitiesDefaultPropertiesFunction.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addExternallyProvidedSensitivitiesDefaultProperties(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ExternallyProvidedSecurityMarkFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesCreditFactorsFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesNonYieldCurveFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveCS01Function.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurvePV01Function.class));
  }

}
