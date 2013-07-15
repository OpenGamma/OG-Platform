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

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the default functions contained in this package.
 */
public class FXForwardPropertiesFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Currency specific data.
   */
  public static class CurrencyInfo implements InitializingBean {
    /** The curve configuration name */
    private String _curveConfiguration;
    /** The discounting curve name */
    private String _discountingCurve;

    /**
     * Gets the curve configuration name.
     * @return The curve configuration name
     */
    public String getCurveConfiguration() {
      return _curveConfiguration;
    }

    /**
     * Sets the curve configuration name.
     * @param curveConfiguration The curve configuration name, not null
     */
    public void setCurveConfiguration(final String curveConfiguration) {
      _curveConfiguration = curveConfiguration;
    }

    /**
     * Gets the discounting curve name
     * @return The discounting curve name
     */
    public String getDiscountingCurve() {
      return _discountingCurve;
    }

    /**
     * Sets the discounting curve name.
     * @param discountingCurve The discounting curve name, not null
     */
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
    /** The forward curve name */
    private String _forwardCurveName;

    /**
     * Gets the forward curve name.
     * @return The forward curve name
     */
    public String getForwardCurveName() {
      return _forwardCurveName;
    }

    /**
     * Sets the forward curve name
     * @param forwardCurveName The forward curve name, not null
     */
    public void setForwardCurveName(final String forwardCurveName) {
      _forwardCurveName = forwardCurveName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getForwardCurveName(), "forwardCurveName");
    }

  }
  /** The per-currency defaults */
  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  /** The per-currency pair defaults */
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();

  /**
   * Sets the defaults for a set of currencies.
   * @param perCurrencyInfo The currency pair defaults
   */
  public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
    _perCurrencyInfo.clear();
    _perCurrencyInfo.putAll(perCurrencyInfo);
  }

  /**
   * Gets the defaults for a currency.
   * @return The currency defaults
   */
  public Map<String, CurrencyInfo> getPerCurrencyInfo() {
    return _perCurrencyInfo;
  }

  /**
   * Sets the defaults for a currency.
   * @param currency The currency
   * @param info The defaults
   */
  public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
    _perCurrencyInfo.put(currency, info);
  }

  /**
   * Gets the defaults for a currency.
   * @param currency The currency
   * @return The defaults
   */
  public CurrencyInfo getCurrencyInfo(final String currency) {
    return _perCurrencyInfo.get(currency);
  }

  /**
   * Sets the defaults for a set of currency pairs.
   * @param perCurrencyPairInfo The currency pairs and defaults
   */
  public void setPerCurrencyPairInfo(final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo) {
    _perCurrencyPairInfo.clear();
    _perCurrencyPairInfo.putAll(perCurrencyPairInfo);
  }

  /**
   * Gets the defaults for a currency pair.
   * @return The defaults
   */
  public Map<Pair<String, String>, CurrencyPairInfo> getPerCurrencyPairInfo() {
    return _perCurrencyPairInfo;
  }

  /**
   * Sets defaults for a currency pair.
   * @param currencyPair The currency pair
   * @param info The currency pair defaults
   */
  public void setCurrencyPairInfo(final Pair<String, String> currencyPair, final CurrencyPairInfo info) {
    _perCurrencyPairInfo.put(currencyPair, info);
  }

  /**
   * Gets defaults for a currency pair.
   * @param currencyPair The currency pair
   * @return The currency pair defaults
   */
  public CurrencyPairInfo getCurrencyPairInfo(final Pair<String, String> currencyPair) {
    return _perCurrencyPairInfo.get(currencyPair);
  }

  /**
   * Adds defaults for FX forwards relevant to the discounting calculation method.
   * @param functions The list of functions
   */
  protected void addFXForwardDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[getPerCurrencyInfo().size() * 3];
    int i = 0;
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      args[i++] = e.getKey();
      args[i++] = e.getValue().getCurveConfiguration();
      args[i++] = e.getValue().getDiscountingCurve();
    }
    functions.add(functionConfiguration(FXForwardDefaults.class, args));
  }

  /**
   * Adds defaults for FX forwards relevant to the forward point calculation method.
   * @param functions The list of functions
   */
  protected void addFXForwardPointsDefaults(final List<FunctionConfiguration> functions) {
    final String[] args = new String[getPerCurrencyPairInfo().size() * 3];
    int i = 0;
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      args[i++] = e.getKey().getFirst();
      args[i++] = e.getKey().getSecond();
      args[i++] = e.getValue().getForwardCurveName();
    }
    functions.add(functionConfiguration(FXForwardForwardPointsDefaults.class, args));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    if (!getPerCurrencyInfo().isEmpty()) {
      addFXForwardDefaults(functions);
    }
    if (!getPerCurrencyPairInfo().isEmpty()) {
      addFXForwardPointsDefaults(functions);
    }
  }

}
