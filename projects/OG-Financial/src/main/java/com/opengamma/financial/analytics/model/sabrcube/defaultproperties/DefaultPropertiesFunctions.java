/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated The functions in this package are deprecated
 */
@Deprecated
public class DefaultPropertiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Currency specific data.
   */
  public static class CurrencyInfo implements InitializingBean {

    private String _curveConfiguration;
    private String _cubeName;

    public String getCurveConfiguration() {
      return _curveConfiguration;
    }

    public void setCurveConfiguration(final String curveConfiguration) {
      _curveConfiguration = curveConfiguration;
    }

    public String getCubeName() {
      return _cubeName;
    }

    public void setCubeName(final String cubeName) {
      _cubeName = cubeName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
      ArgumentChecker.notNullInjected(getCubeName(), "cubeName");
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  private String _fittingMethod = SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES;
  private String _xInterpolator = Interpolator1DFactory.LINEAR;
  private String _xLeftExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _xRightExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _yInterpolator = Interpolator1DFactory.LINEAR;
  private String _yLeftExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private String _yRightExtrapolator = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private double _cutOff = 0.07;
  private double _mu = 10.0;

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

  public void setFittingMethod(final String fittingMethod) {
    _fittingMethod = fittingMethod;
  }

  public String getFittingMethod() {
    return _fittingMethod;
  }

  public String getXInterpolator() {
    return _xInterpolator;
  }

  public void setXInterpolator(final String xInterpolator) {
    _xInterpolator = xInterpolator;
  }

  public String getXLeftExtrapolator() {
    return _xLeftExtrapolator;
  }

  public void setXLeftExtrapolator(final String xLeftExtrapolator) {
    _xLeftExtrapolator = xLeftExtrapolator;
  }

  public String getXRightExtrapolator() {
    return _xRightExtrapolator;
  }

  public void setXRightExtrapolator(final String xRightExtrapolator) {
    _xRightExtrapolator = xRightExtrapolator;
  }

  public String getYInterpolator() {
    return _yInterpolator;
  }

  public void setYInterpolator(final String yInterpolator) {
    _yInterpolator = yInterpolator;
  }

  public String getYLeftExtrapolator() {
    return _yLeftExtrapolator;
  }

  public void setYLeftExtrapolator(final String yLeftExtrapolator) {
    _yLeftExtrapolator = yLeftExtrapolator;
  }

  public String getYRightExtrapolator() {
    return _yRightExtrapolator;
  }

  public void setYRightExtrapolator(final String yRightExtrapolator) {
    _yRightExtrapolator = yRightExtrapolator;
  }

  public void setCutOff(final double cutOff) {
    _cutOff = cutOff;
  }

  public double getCutOff() {
    return _cutOff;
  }

  public void setMu(final double mu) {
    _mu = mu;
  }

  public double getMu() {
    return _mu;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getFittingMethod(), "fittingMethod");
    ArgumentChecker.notNullInjected(getXInterpolator(), "xInterpolator");
    ArgumentChecker.notNullInjected(getXLeftExtrapolator(), "xLeftExtrapolator");
    ArgumentChecker.notNullInjected(getXRightExtrapolator(), "xRightExtrapolator");
    ArgumentChecker.notNullInjected(getYInterpolator(), "yInterpolator");
    ArgumentChecker.notNullInjected(getYLeftExtrapolator(), "yLeftExtrapolator");
    ArgumentChecker.notNullInjected(getYRightExtrapolator(), "yRightExtrapolator");
    super.afterPropertiesSet();
  }

  protected void addNoExtrapolationDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = getFittingMethod();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getCubeName();
    }
    functions.add(functionConfiguration(SABRNoExtrapolationDefaults.class, args));
  }

  protected void addNoExtrapolationVegaDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[7 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = getFittingMethod();
    args[i++] = getXInterpolator();
    args[i++] = getXLeftExtrapolator();
    args[i++] = getXRightExtrapolator();
    args[i++] = getYInterpolator();
    args[i++] = getYLeftExtrapolator();
    args[i++] = getYRightExtrapolator();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getCubeName();
    }
    functions.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, args));
  }

  protected void addNonLinearLeastSquaresSwaptionCubeFittingDefaults(final List<FunctionConfiguration> functions) {
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      functions.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, e.getKey(), e.getValue().getCubeName()));
    }
  }

  protected void addRightExtrapolationDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[3 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = getFittingMethod();
    args[i++] = Double.toString(getCutOff());
    args[i++] = Double.toString(getMu());
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getCubeName();
    }
    functions.add(functionConfiguration(SABRRightExtrapolationDefaults.class, args));
  }

  protected void addRightExtrapolationVegaDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[9 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = getFittingMethod();
    args[i++] = Double.toString(getCutOff());
    args[i++] = Double.toString(getMu());
    args[i++] = getXInterpolator();
    args[i++] = getXLeftExtrapolator();
    args[i++] = getXRightExtrapolator();
    args[i++] = getYInterpolator();
    args[i++] = getYLeftExtrapolator();
    args[i++] = getYRightExtrapolator();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getCubeName();
    }
    functions.add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, args));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    if (!getPerCurrencyInfo().isEmpty()) {
      addNoExtrapolationDefaults(functions);
      addNoExtrapolationVegaDefaults(functions);
      addNonLinearLeastSquaresSwaptionCubeFittingDefaults(functions);
      addRightExtrapolationDefaults(functions);
      addRightExtrapolationVegaDefaults(functions);
    }
  }

}
