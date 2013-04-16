/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DefaultPropertiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Currency specific data.
   */
  public static class CurrencyInfo implements InitializingBean {

    private String _curveConfiguration;
    private String _discountingCurve;

    public String getCurveConfiguration() {
      return _curveConfiguration;
    }

    public void setCurveConfiguration(final String curveConfiguration) {
      _curveConfiguration = curveConfiguration;
    }

    public String getDiscountingCurve() {
      return _discountingCurve;
    }

    public void setDiscountingCurve(final String discountingCurve) {
      _discountingCurve = discountingCurve;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
      ArgumentChecker.notNullInjected(getDiscountingCurve(), "discountingCurve");
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
  private double _theta = 0.5;
  private int _nTimeSteps = 100;
  private int _nSpaceSteps = 100;
  private double _timeStepBunching = 5.0;
  private double _spaceStepBunching = 0.05;
  private double _maxProxyDelta = 1.5;
  private double _centreMoneyness = 1.0;
  private double _maxMoneynessScale = 3.5;
  private String _spaceDirectionInterpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;

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

  public void setTheta(final double theta) {
    _theta = theta;
  }

  public double getTheta() {
    return _theta;
  }

  public void setNTimeSteps(final int nTimeSteps) {
    _nTimeSteps = nTimeSteps;
  }

  public int getNTimeSteps() {
    return _nTimeSteps;
  }

  public void setNSpaceSteps(final int nSpaceSteps) {
    _nSpaceSteps = nSpaceSteps;
  }

  public int getNSpaceSteps() {
    return _nSpaceSteps;
  }

  public void setTimeStepBunching(final double timeStepBunching) {
    _timeStepBunching = timeStepBunching;
  }

  public double getTimeStepBunching() {
    return _timeStepBunching;
  }

  public void setSpaceStepBunching(final double spaceStepBunching) {
    _spaceStepBunching = spaceStepBunching;
  }

  public double getSpaceStepBunching() {
    return _spaceStepBunching;
  }

  public void setMaxProxyDelta(final double maxProxyDelta) {
    _maxProxyDelta = maxProxyDelta;
  }

  public double getMaxProxyDelta() {
    return _maxProxyDelta;
  }

  public void setCentreMoneyness(final double centreMoneyness) {
    _centreMoneyness = centreMoneyness;
  }

  public double getCentreMoneyness() {
    return _centreMoneyness;
  }

  public void setMaxMoneynessScale(final double maxMoneynessScale) {
    _maxMoneynessScale = maxMoneynessScale;
  }

  public double getMaxMoneynessScale() {
    return _maxMoneynessScale;
  }

  public void setSpaceDirectionInterpolator(final String spaceDirectionInterpolator) {
    _spaceDirectionInterpolator = spaceDirectionInterpolator;
  }

  public String getSpaceDirectionInterpolator() {
    return _spaceDirectionInterpolator;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getSpaceDirectionInterpolator(), "spaceDirectionInterpolator");
    super.afterPropertiesSet();
  }

  protected void addPDECurveDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[getPerCurrencyInfo().size() * 3];
    int i = 0;
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getDiscountingCurve();
      args[i++] = e.getValue().getCurveConfiguration();
    }
    functions.add(functionConfiguration(FXPDECurveDefaults.class, args));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForwardPDEDefaults.class, Double.toString(getTheta()), Integer.toString(getNTimeSteps()), Integer.toString(getNSpaceSteps()),
        Double.toString(getTimeStepBunching()), Double.toString(getSpaceStepBunching()), Double.toString(getMaxProxyDelta()), Double.toString(getCentreMoneyness()), getSpaceDirectionInterpolator()));
    functions.add(functionConfiguration(BackwardPDEDefaults.class, Double.toString(getTheta()), Integer.toString(getNTimeSteps()), Integer.toString(getNSpaceSteps()),
        Double.toString(getTimeStepBunching()), Double.toString(getSpaceStepBunching()), Double.toString(getMaxMoneynessScale()), getSpaceDirectionInterpolator()));
    if (!getPerCurrencyInfo().isEmpty()) {
      addPDECurveDefaults(functions);
    }
  }

}
