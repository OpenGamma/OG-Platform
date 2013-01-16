/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureOptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new FutureOptionFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveName;
      private String _curveCalculationConfig;
      private String _surfaceName;
      private String _interpolationMethod = "Spline";

      public String getCurveName() {
        return _curveName;
      }

      public void setCurveName(final String curveName) {
        _curveName = curveName;
      }

      public String getCurveCalculationConfig() {
        return _curveCalculationConfig;
      }

      public void setCurveCalculationConfig(final String curveCalculationConfig) {
        _curveCalculationConfig = curveCalculationConfig;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getInterpolationMethod() {
        return _interpolationMethod;
      }

      public void setInterpolationMethod(final String interpolationMethod) {
        _interpolationMethod = interpolationMethod;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveName(), "curveName");
        ArgumentChecker.notNullInjected(getCurveCalculationConfig(), "curveCalculationConfig");
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
        ArgumentChecker.notNullInjected(getInterpolationMethod(), "interpolationMethod");
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

    protected void addCommodityFutureOptionBlackDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 5];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveName();
        args[i++] = e.getValue().getCurveCalculationConfig();
        args[i++] = e.getValue().getSurfaceName();
        args[i++] = e.getValue().getInterpolationMethod();
      }
      functions.add(functionConfiguration(CommodityFutureOptionBlackDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addCommodityFutureOptionBlackDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CommodityFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackForwardDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackForwardGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandGreeksFunction.class));
  }

}
