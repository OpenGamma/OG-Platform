/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.horizon.SwaptionBlackThetaDefaults;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated The functions that are added are deprecated
 */
@Deprecated
public class BlackFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BlackFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfig;
      private String _surfaceName;

      public void setCurveConfig(final String curveConfig) {
        _curveConfig = curveConfig;
      }

      public String getCurveConfig() {
        return _curveConfig;
      }

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfig(), "curveConfig");
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    private int _numberOfDays = 1;

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

    public void setNumberOfDays(final int numberOfDays) {
      _numberOfDays = numberOfDays;
    }

    public int getNumberOfDays() {
      return _numberOfDays;
    }

    protected void addSwaptionBlackDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfig();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(SwaptionBlackDefaultPropertiesFunction.class, args));
    }

    protected void addSwaptionBlackThetaDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = Integer.toString(getNumberOfDays());
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfig();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(SwaptionBlackThetaDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addSwaptionBlackDefaults(functions);
        addSwaptionBlackThetaDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(SwaptionBlackVolatilitySensitivityFunction.class));
    functions.add(functionConfiguration(SwaptionBlackImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(SwaptionBlackTheoreticaForwardDeltaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackTheoreticalForwardGammaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackDriftlessThetaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackTheoreticalForwardThetaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackTheoreticalForwardVegaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackValueThetaFunction.class));
    functions.add(functionConfiguration(SwaptionBlackForwardFunction.class));
  }

}
