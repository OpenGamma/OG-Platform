/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.List;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DefaultPropertiesFunctions extends AbstractRepositoryConfigurationBean {

  // TODO: Currency and/or CurrencyPair specific data

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

  // TODO: Currency and/or CurrencyPair specific data

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

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackVolatilitySurfaceSABRDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getSabrModel(), getWeightingFunction(), isUseExternalBeta() ? "true" : "false", Double.toString(getExternalBeta())));
    functions.add(functionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getWeightingFunction()));
    functions.add(functionConfiguration(BlackVolatilitySurfaceSplineDefaults.class, getTimeAxis(), getYAxis(), getVolatilityTransform(), getTimeInterpolator(), getTimeLeftExtrapolator(),
        getTimeRightExtrapolator(), getSplineInterpolator(), getSplineLeftExtrapolator(), getSplineRightExtrapolator(), getSplineExtrapolatorFailBehaviour()));
  }
}
