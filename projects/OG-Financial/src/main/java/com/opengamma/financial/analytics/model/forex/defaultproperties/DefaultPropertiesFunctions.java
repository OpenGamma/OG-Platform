/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the default functions contained in this package.
 */
public class DefaultPropertiesFunctions extends AbstractRepositoryConfigurationBean {

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

  /**
   * Currency-pair specific data.
   */
  public static class CurrencyPairInfo implements InitializingBean {

    private String _surfaceName;

    public void setSurfaceName(final String surfaceName) {
      _surfaceName = surfaceName;
    }

    public String getSurfaceName() {
      return _surfaceName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<Pair<String, String>, CurrencyPairInfo>();
  private String _interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private String _leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private String _rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

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

  public void setInterpolatorName(final String interpolatorName) {
    _interpolatorName = interpolatorName;
  }

  public String getInterpolatorName() {
    return _interpolatorName;
  }

  public void setLeftExtrapolatorName(final String leftExtrapolatorName) {
    _leftExtrapolatorName = leftExtrapolatorName;
  }

  public String getLeftExtrapolatorName() {
    return _leftExtrapolatorName;
  }

  public void setRightExtrapolatorName(final String rightExtrapolatorName) {
    _rightExtrapolatorName = rightExtrapolatorName;
  }

  public String getRightExtrapolatorName() {
    return _rightExtrapolatorName;
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getInterpolatorName(), "interpolatorName");
    ArgumentChecker.notNullInjected(getLeftExtrapolatorName(), "leftExtrapolatorName");
    ArgumentChecker.notNullInjected(getRightExtrapolatorName(), "rightExtrapolatorName");
    super.afterPropertiesSet();
  }

  protected void addFXForwardDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = PriorityClass.NORMAL.name();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getDiscountingCurve();
    }
    functions.add(functionConfiguration(FXForwardDefaults.class, args));
  }

  protected void addFXOptionBlackCurveDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
    int i = 0;
    args[i++] = PriorityClass.NORMAL.name();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getDiscountingCurve();
    }
    functions.add(functionConfiguration(FXOptionBlackCurveDefaults.class, args));
  }

  protected void addFXOptionBlackSurfaceDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[4 + getPerCurrencyPairInfo().size() * 3];
    int i = 0;
    args[i++] = PriorityClass.NORMAL.name();
    args[i++] = getInterpolatorName();
    args[i++] = getLeftExtrapolatorName();
    args[i++] = getRightExtrapolatorName();
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      args[i++] = e.getKey().getFirst();
      args[i++] = e.getKey().getSecond();
      args[i++] = e.getValue().getSurfaceName();
    }
    functions.add(functionConfiguration(FXOptionBlackSurfaceDefaults.class, args));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    if (!getPerCurrencyInfo().isEmpty()) {
      addFXForwardDefaults(functions);
      addFXOptionBlackCurveDefaults(functions);
    }
    if (!getPerCurrencyPairInfo().isEmpty()) {
      addFXOptionBlackSurfaceDefaults(functions);
    }
  }

}
