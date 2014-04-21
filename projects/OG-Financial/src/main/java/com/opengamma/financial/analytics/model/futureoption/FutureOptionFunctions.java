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

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureOptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new FutureOptionFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveName;
      private String _curveCalculationConfig;
      private String _surfaceName;
      private String _interpolationMethod = "Spline";
      private String _forwardCurveName;
      private String _forwardCurveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD;
      private String _surfaceCalculationMethod = BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL;

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

      public String getForwardCurveName() {
        return _forwardCurveName;
      }

      public void setForwardCurveName(final String forwardCurveName) {
        _forwardCurveName = forwardCurveName;
      }

      public String getForwardCurveCalculationMethodName() {
        return _forwardCurveCalculationMethod;
      }

      public void setForwardCurveCalculationMethodName(final String forwardCurveCalculationMethod) {
        _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
      }

      public String getSurfaceCalculationMethod() {
        return _surfaceCalculationMethod;
      }

      public void setSurfaceCalculationMethod(final String surfaceCalculationMethod) {
        _surfaceCalculationMethod = surfaceCalculationMethod;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveName(), "curveName");
        ArgumentChecker.notNullInjected(getCurveCalculationConfig(), "curveCalculationConfig");
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
        ArgumentChecker.notNullInjected(getSurfaceCalculationMethod(), "surface calculation method");
        ArgumentChecker.notNullInjected(getInterpolationMethod(), "interpolationMethod");
        ArgumentChecker.notNullInjected(getForwardCurveName(), "forward curve name");
        ArgumentChecker.notNullInjected(getForwardCurveCalculationMethodName(), "forward curve calculation method name");
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

    protected void addCommodityFutureOptionDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 2 + 1];
      args[0] = PriorityClass.NORMAL.name();
      int i = 1;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        final CurrencyInfo value = e.getValue();
        args[i++] = value.getSurfaceCalculationMethod();
      }
      functions.add(functionConfiguration(CommodityFutureOptionSurfaceCalculationMethodDefaults.class, args));
    }

    protected void addCommodityFutureOptionSurfaceCalculationMethodDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 7 + 1];
      args[0] = PriorityClass.NORMAL.name();
      int i = 1;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        final CurrencyInfo value = e.getValue();
        args[i++] = value.getCurveName();
        args[i++] = value.getCurveCalculationConfig();
        args[i++] = value.getSurfaceName();
        args[i++] = value.getInterpolationMethod();
        args[i++] = value.getForwardCurveName();
        args[i++] = value.getForwardCurveCalculationMethodName();
      }
      functions.add(functionConfiguration(CommodityFutureOptionBlackLognormalDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addCommodityFutureOptionSurfaceCalculationMethodDefaults(functions);
        addCommodityFutureOptionDefaults(functions);
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
    functions.add(functionConfiguration(CommodityFutureOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWValueDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWValueGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackPVFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackValueThetaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBlackValueVegaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBAWPVFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBAWValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionBAWValueGammaFunction.class));
  }

}
