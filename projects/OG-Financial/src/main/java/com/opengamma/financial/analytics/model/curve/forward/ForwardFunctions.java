/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForwardFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new ForwardFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfiguration;
      private String _discountingCurve;
      private String _forwardCurve;

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

      public String getForwardCurve() {
        return _forwardCurve;
      }

      public void setForwardCurve(final String forwardCurve) {
        _forwardCurve = forwardCurve;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
        ArgumentChecker.notNullInjected(getDiscountingCurve(), "discountingCurve");
        ArgumentChecker.notNullInjected(getForwardCurve(), "forwardCurve");
      }

    }

    /**
     * Currency pair specific data.
     */
    public static class CurrencyPairInfo implements InitializingBean {

      private String _curveName;
      private String _curveCalculationMethod = ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD;

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

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveName(), "curveName");
        ArgumentChecker.notNullInjected(getCurveCalculationMethod(), "curveCalculationMethod");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();
    private String _interpolator = "DoubleQuadratic";
    private String _leftExtrapolator = "LinearExtrapolator";
    private String _rightExtrapolator = "FlatExtrapolator";

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

    public void setInterpolator(final String interpolator) {
      _interpolator = interpolator;
    }

    public String getInterpolator() {
      return _interpolator;
    }

    public void setLeftExtrapolator(final String leftExtrapolator) {
      _leftExtrapolator = leftExtrapolator;
    }

    public String getLeftExtrapolator() {
      return _leftExtrapolator;
    }

    public void setRightExtrapolator(final String rightExtrapolator) {
      _rightExtrapolator = rightExtrapolator;
    }

    public String getRightExtrapolator() {
      return _rightExtrapolator;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getInterpolator(), "interpolator");
      ArgumentChecker.notNullInjected(getLeftExtrapolator(), "leftExtrapolator");
      ArgumentChecker.notNullInjected(getRightExtrapolator(), "rightExtrapolator");
      super.afterPropertiesSet();
    }

    protected void addForwardCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getDiscountingCurve();
      }
      functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesPrimitiveDefaults.class, args));
      functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesSecurityDefaults.class, args));
    }

    protected void addFXForwardCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyPairInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
        final String ccy1 = e.getKey().getFirst();
        final String ccy2 = e.getKey().getSecond();
        if (ccy1.compareTo(ccy2) <= 0) {
          args[i++] = ccy1 + ccy2;
        } else {
          args[i++] = ccy2 + ccy1;
        }
        args[i++] = e.getValue().getCurveName();
        args[i++] = e.getValue().getCurveCalculationMethod();
      }
      functions.add(functionConfiguration(FXForwardCurvePrimitiveDefaults.class, args));
      functions.add(functionConfiguration(FXForwardCurveSecurityDefaults.class, args));
      functions.add(functionConfiguration(FXForwardCurveTradeDefaults.class, args));
    }

    /**
     * Adds defaults for functions that calculate a commodity forward curve using future quotes.
     * @param functions The list of functions
     */
    protected void addCommodityForwardFromFutureCurvePerCurrencyDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3 + 1];
      args[0] = PriorityClass.ABOVE_NORMAL.name();
      int i = 1;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getForwardCurve();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(CommodityForwardCurveFromFuturePerCurrencyDefaults.class, args));
    }

    /**
     * Adds defaults for functions that calculate an equity forward curve using future quotes.
     * @param functions The list of functions
     */
    protected void addEquityForwardFromFutureCurvePerCurrencyDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3 + 1];
      args[0] = PriorityClass.ABOVE_NORMAL.name();
      int i = 1;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getForwardCurve();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(EquityIndexForwardCurveFromFuturePerCurrencyDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesDefaults.class, getInterpolator(), getLeftExtrapolator(), getRightExtrapolator()));
      functions.add(functionConfiguration(InterpolatedForwardCurveDefaults.class, getInterpolator(), getLeftExtrapolator(), getRightExtrapolator()));
      if (!getPerCurrencyInfo().isEmpty()) {
        addForwardCurveDefaults(functions);
        addCommodityForwardFromFutureCurvePerCurrencyDefaults(functions);
        addEquityForwardFromFutureCurvePerCurrencyDefaults(functions);
      }
      if (!getPerCurrencyPairInfo().isEmpty()) {
        addFXForwardCurveDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForwardSwapCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(ForwardSwapCurveMarketDataFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesFunction.class));
    functions.add(functionConfiguration(FXForwardCurveMarketDataFunction.class));
    functions.add(functionConfiguration(FXForwardPointsCurveMarketDataFunction.class));
    functions.add(functionConfiguration(CommodityForwardCurveFromFutureCurveFunction.class));
    functions.add(functionConfiguration(EquityIndexForwardCurveFromFutureCurveFunction.class));
  }
}
