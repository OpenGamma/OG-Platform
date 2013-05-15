/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DefaultPropertiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Currency specific data.
   */
  public static class CurrencyInfo implements InitializingBean {

    private String _curveName;
    private String _curveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD;
    private String _surfaceName;

    public String getCurveName() {
      return _curveName;
    }

    public void setCurveName(final String curveName) {
      _curveName = curveName;
    }

    public String getCurveCalculationMethod() {
      return _curveCalculationMethod;
    }

    public void setCurveCalculationMethod(final String curveCalculationMethod) {
      _curveCalculationMethod = curveCalculationMethod;
    }

    public String getSurfaceName() {
      return _surfaceName;
    }

    public void setSurfaceName(final String surfaceName) {
      _surfaceName = surfaceName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCurveName(), "curveName");
      ArgumentChecker.notNullInjected(getCurveCalculationMethod(), "curveCalculationMethod");
      ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
    }

  }

  /**
   * Currency-pair specific data.
   */
  public static class CurrencyPairInfo implements InitializingBean {

    private String _curveName;
    private String _curveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD;
    private String _surfaceName;

    public void setCurveName(final String curveName) {
      _curveName = curveName;
    }

    public String getCurveName() {
      return _curveName;
    }

    public void setCurveCalculationMethod(final String curveCalculationMethod) {
      _curveCalculationMethod = curveCalculationMethod;
    }

    public String getCurveCalculationMethod() {
      return _curveCalculationMethod;
    }

    public void setSurfaceName(final String surfaceName) {
      _surfaceName = surfaceName;
    }

    public String getSurfaceName() {
      return _surfaceName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCurveName(), "curveName");
      ArgumentChecker.notNullInjected(getCurveCalculationMethod(), "curveCalculationMethod");
      ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();
  private String _timeAxis = BlackVolatilitySurfacePropertyNamesAndValues.LOG_TIME;
  private String _yAxis = BlackVolatilitySurfacePropertyNamesAndValues.LOG_Y;
  private String _volatilityTransform = BlackVolatilitySurfacePropertyNamesAndValues.INTEGRATED_VARIANCE;
  private String _timeInterpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private String _timeLeftExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _timeRightExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _sabrModel = VolatilityFunctionFactory.HAGAN;
  private String _weightingFunction = WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME;
  private boolean _useExternalBeta /*= false*/;
  private double _externalBeta = 0.5;
  private String _splineInterpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private String _splineLeftExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _splineRightExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _splineExtrapolatorFailBehaviour = BlackVolatilitySurfacePropertyNamesAndValues.FLAT_SPLINE_EXTRAPOLATOR_FAILURE;

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

  public void setPerCurrencyPairInfo(final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo) {
    _perCurrencyPairInfo.clear();
    _perCurrencyPairInfo.putAll(perCurrencyPairInfo);
  }

  public Map<Pair<String, String>, CurrencyPairInfo> getPerCurrencyPairInfo() {
    return _perCurrencyPairInfo;
  }

  public void setCurrencyPairInfo(final Pair<String, String> currencyPair, final CurrencyPairInfo info) {
    _perCurrencyPairInfo.put(currencyPair, info);
  }

  public CurrencyPairInfo getCurrencyPairInfo(final Pair<String, String> currencyPair) {
    return _perCurrencyPairInfo.get(currencyPair);
  }

  public String getTimeAxis() {
    return _timeAxis;
  }

  public void setTimeAxis(final String timeAxis) {
    _timeAxis = timeAxis;
  }

  public String getYAxis() {
    return _yAxis;
  }

  public void setYAxis(final String yAxis) {
    _yAxis = yAxis;
  }

  public String getVolatilityTransform() {
    return _volatilityTransform;
  }

  public void setVolatilityTransform(final String volatilityTransform) {
    _volatilityTransform = volatilityTransform;
  }

  public String getTimeInterpolator() {
    return _timeInterpolator;
  }

  public void setTimeInterpolator(final String timeInterpolator) {
    _timeInterpolator = timeInterpolator;
  }

  public String getTimeLeftExtrapolator() {
    return _timeLeftExtrapolator;
  }

  public void setTimeLeftExtrapolator(final String timeLeftExtrapolator) {
    _timeLeftExtrapolator = timeLeftExtrapolator;
  }

  public String getTimeRightExtrapolator() {
    return _timeRightExtrapolator;
  }

  public void setTimeRightExtrapolator(final String timeRightExtrapolator) {
    _timeRightExtrapolator = timeRightExtrapolator;
  }

  public String getSabrModel() {
    return _sabrModel;
  }

  public void setSabrModel(final String sabrModel) {
    _sabrModel = sabrModel;
  }

  public String getWeightingFunction() {
    return _weightingFunction;
  }

  public void setWeightingFunction(final String weightingFunction) {
    _weightingFunction = weightingFunction;
  }

  public boolean isUseExternalBeta() {
    return _useExternalBeta;
  }

  public void setUseExternalBeta(final boolean useExternalBeta) {
    _useExternalBeta = useExternalBeta;
  }

  public double getExternalBeta() {
    return _externalBeta;
  }

  public void setExternalBeta(final double externalBeta) {
    _externalBeta = externalBeta;
  }

  public String getSplineInterpolator() {
    return _splineInterpolator;
  }

  public void setSplineInterpolator(final String splineInterpolator) {
    _splineInterpolator = splineInterpolator;
  }

  public String getSplineLeftExtrapolator() {
    return _splineLeftExtrapolator;
  }

  public void setSplineLeftExtrapolator(final String splineLeftExtrapolator) {
    _splineLeftExtrapolator = splineLeftExtrapolator;
  }

  public String getSplineRightExtrapolator() {
    return _splineRightExtrapolator;
  }

  public void setSplineRightExtrapolator(final String splineRightExtrapolator) {
    _splineRightExtrapolator = splineRightExtrapolator;
  }

  public String getSplineExtrapolatorFailBehaviour() {
    return _splineExtrapolatorFailBehaviour;
  }

  public void setSplineExtrapolatorFailBehaviour(final String splineExtrapolatorFailBehaviour) {
    _splineExtrapolatorFailBehaviour = splineExtrapolatorFailBehaviour;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getTimeAxis(), "timeAxis");
    ArgumentChecker.notNullInjected(getYAxis(), "yAxis");
    ArgumentChecker.notNullInjected(getVolatilityTransform(), "volatilityTransform");
    ArgumentChecker.notNullInjected(getTimeInterpolator(), "timeInterpolator");
    ArgumentChecker.notNullInjected(getTimeLeftExtrapolator(), "timeLeftExtrapolator");
    ArgumentChecker.notNullInjected(getTimeRightExtrapolator(), "timeRightExtrapolator");
    ArgumentChecker.notNullInjected(getSabrModel(), "sabrModel");
    ArgumentChecker.notNullInjected(getWeightingFunction(), "weightingFunction");
    ArgumentChecker.notNullInjected(getSplineInterpolator(), "splineInterpolator");
    ArgumentChecker.notNullInjected(getSplineLeftExtrapolator(), "splineLeftExtrapolator");
    ArgumentChecker.notNullInjected(getSplineRightExtrapolator(), "splineRightExtrapolator");
    ArgumentChecker.notNullInjected(getSplineExtrapolatorFailBehaviour(), "splineExtrapolatorFailBehaviour");
    super.afterPropertiesSet();
  }

  protected void addCommodityBlackDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[getPerCurrencyInfo().size() * 4];
    int i = 0;
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveName();
      args[i++] = e.getValue().getCurveCalculationMethod();
      args[i++] = e.getValue().getSurfaceName();
    }
    functions.add(functionConfiguration(CommodityBlackVolatilitySurfacePrimitiveDefaults.class, args));
    functions.add(functionConfiguration(CommodityBlackVolatilitySurfaceSecurityDefaults.class, args));
    functions.add(functionConfiguration(CommodityBlackVolatilitySurfaceTradeDefaults.class, args));
  }

  protected void addFXOptionBlackDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[getPerCurrencyPairInfo().size() * 4];
    int i = 0;
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      args[i++] = (e.getKey().getFirst().compareTo(e.getKey().getSecond()) < 0) ? e.getKey().getFirst() + e.getKey().getSecond() : e.getKey().getSecond() + e.getKey().getFirst();
      args[i++] = e.getValue().getCurveName();
      args[i++] = e.getValue().getCurveCalculationMethod();
      args[i++] = e.getValue().getSurfaceName();
    }
    functions.add(functionConfiguration(FXBlackVolatilitySurfacePrimitiveDefaults.class, args));
    functions.add(functionConfiguration(FXBlackVolatilitySurfaceSecurityDefaults.class, args));
    functions.add(functionConfiguration(FXBlackVolatilitySurfaceTradeDefaults.class, args));

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getWeightingFunction()));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSABRDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getSabrModel(), getWeightingFunction(), isUseExternalBeta() ? "true" : "false", Double.toString(getExternalBeta())));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSplineDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getSplineInterpolator(), getSplineLeftExtrapolator(), getSplineRightExtrapolator(), getSplineExtrapolatorFailBehaviour()));
    if (!getPerCurrencyInfo().isEmpty()) {
      addCommodityBlackDefaults(functions);
    }
    if (!getPerCurrencyPairInfo().isEmpty()) {
      addFXOptionBlackDefaults(functions);
    }
  }
}
