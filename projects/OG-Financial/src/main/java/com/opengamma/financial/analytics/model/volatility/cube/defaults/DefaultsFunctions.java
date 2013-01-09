/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube.defaults;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DefaultsFunctions extends AbstractRepositoryConfigurationBean {

  private double _alphaStartValue = 0.05;
  private double _betaStartValue = 0.5;
  private double _rhoStartValue = 0.7;
  private double _nuStartValue = 0.3;
  private boolean _useFixedAlpha; /* = false;*/
  private boolean _useFixedBeta = true;
  private boolean _useFixedRho; /* = false;*/
  private boolean _useFixedNu; /* = false;*/
  private double _eps = 0.001;
  private String _xInterpolator = "Linear";
  private String _xExtrapolator = "FlatExtrapolator";
  private String _yInterpolator = "Linear";
  private String _yExtrapolator = "FlatExtrapolator";
  private String _forwardCurveCalculationMethod = "ForwardSwapQuotes";
  private String _forwardCurveInterpolator = "DoubleQuadratic";
  private String _forwardCurveLeftExtrapolator = "LinearExtrapolator";
  private String _forwardCurveRightExtrapolator = "LinearExtrapolator";

  public double getAlphaStartValue() {
    return _alphaStartValue;
  }

  public void setAlphaStartValue(final double alphaStartValue) {
    _alphaStartValue = alphaStartValue;
  }

  public double getBetaStartValue() {
    return _betaStartValue;
  }

  public void setBetaStartValue(final double betaStartValue) {
    _betaStartValue = betaStartValue;
  }

  public double getRhoStartValue() {
    return _rhoStartValue;
  }

  public void setRhoStartValue(final double rhoStartValue) {
    _rhoStartValue = rhoStartValue;
  }

  public double getNuStartValue() {
    return _nuStartValue;
  }

  public void setNuStartValue(final double nuStartValue) {
    _nuStartValue = nuStartValue;
  }

  public boolean isUseFixedAlpha() {
    return _useFixedAlpha;
  }

  public void setUseFixedAlpha(final boolean useFixedAlpha) {
    _useFixedAlpha = useFixedAlpha;
  }

  public boolean isUseFixedBeta() {
    return _useFixedBeta;
  }

  public void setUseFixedBeta(final boolean useFixedBeta) {
    _useFixedBeta = useFixedBeta;
  }

  public boolean isUseFixedRho() {
    return _useFixedRho;
  }

  public void setUseFixedRho(final boolean useFixedRho) {
    _useFixedRho = useFixedRho;
  }

  public boolean isUseFixedNu() {
    return _useFixedNu;
  }

  public void setUseFixedNu(final boolean useFixedNu) {
    _useFixedNu = useFixedNu;
  }

  public double getEps() {
    return _eps;
  }

  public void setEps(final double eps) {
    _eps = eps;
  }

  public String getXInterpolator() {
    return _xInterpolator;
  }

  public void setXInterpolator(final String xInterpolator) {
    _xInterpolator = xInterpolator;
  }

  public String getXExtrapolator() {
    return _xExtrapolator;
  }

  public void setXExtrapolator(final String xExtrapolator) {
    _xExtrapolator = xExtrapolator;
  }

  public String getYInterpolator() {
    return _yInterpolator;
  }

  public void setYInterpolator(final String yInterpolator) {
    _yInterpolator = yInterpolator;
  }

  public String getYExtrapolator() {
    return _yExtrapolator;
  }

  public void setYExtrapolator(final String yExtrapolator) {
    _yExtrapolator = yExtrapolator;
  }

  public String getForwardCurveCalculationMethod() {
    return _forwardCurveCalculationMethod;
  }

  public void setForwardCurveCalculationMethod(final String forwardCurveCalculationMethod) {
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
  }

  public String getForwardCurveInterpolator() {
    return _forwardCurveInterpolator;
  }

  public void setForwardCurveInterpolator(final String forwardCurveInterpolator) {
    _forwardCurveInterpolator = forwardCurveInterpolator;
  }

  public String getForwardCurveLeftExtrapolator() {
    return _forwardCurveLeftExtrapolator;
  }

  public void setForwardCurveLeftExtrapolator(final String forwardCurveLeftExtrapolator) {
    _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
  }

  public String getForwardCurveRightExtrapolator() {
    return _forwardCurveRightExtrapolator;
  }

  public void setForwardCurveRightExtrapolator(final String forwardCurveRightExtrapolator) {
    _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getXInterpolator(), "xInterpolator");
    ArgumentChecker.notNullInjected(getXExtrapolator(), "xExtrapolator");
    ArgumentChecker.notNullInjected(getYInterpolator(), "yInterpolator");
    ArgumentChecker.notNullInjected(getYExtrapolator(), "yExtrapolator");
    ArgumentChecker.notNullInjected(getForwardCurveCalculationMethod(), "forwardCurveCalculationMethod");
    ArgumentChecker.notNullInjected(getForwardCurveInterpolator(), "forwardCurveInterpolator");
    ArgumentChecker.notNullInjected(getForwardCurveLeftExtrapolator(), "forwardCurveLeftExtrapolator");
    ArgumentChecker.notNullInjected(getForwardCurveRightExtrapolator(), "forwardCurveRightExtrapolator");
    super.afterPropertiesSet();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(SABRNonLinearSwaptionVolatilityCubeFittingDefaults.class, Double.toString(getAlphaStartValue()), Double.toString(getBetaStartValue()),
        Double.toString(getRhoStartValue()), Double.toString(getNuStartValue()), Boolean.toString(isUseFixedAlpha()), Boolean.toString(isUseFixedBeta()), Boolean.toString(isUseFixedRho()),
        Boolean.toString(isUseFixedNu()), Double.toString(getEps()), getXInterpolator(), getXExtrapolator(), getYInterpolator(), getYExtrapolator(), getForwardCurveCalculationMethod(),
        getForwardCurveInterpolator(), getForwardCurveLeftExtrapolator(), getForwardCurveRightExtrapolator()));
  }

}
