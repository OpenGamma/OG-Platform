/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class PNLFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new PNLFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class DeprecatedFunctions extends AbstractFunctionConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(YieldCurveNodePnLFunctionDeprecated.class));
    }

  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractFunctionConfigurationBean {

    private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
    private String _mark2MarketField;
    private String _costOfCarryField;
    private String _valueFieldName = MarketDataRequirementNames.MARKET_VALUE;

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    public void setMark2MarketField(final String mark2MarketField) {
      _mark2MarketField = mark2MarketField;
    }

    public String getMark2MarketField() {
      return _mark2MarketField;
    }

    public void setCostOfCarryField(final String costOfCarryField) {
      _costOfCarryField = costOfCarryField;
    }

    public String getCostOfCarryField() {
      return _costOfCarryField;
    }

    public void setValueFieldName(final String valueFieldName) {
      _valueFieldName = valueFieldName;
    }

    public String getValueFieldName() {
      return _valueFieldName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getHtsResolutionKey(), "htsResolutionKey");
      ArgumentChecker.notNullInjected(getMark2MarketField(), "mark2MarketField");
      ArgumentChecker.notNullInjected(getCostOfCarryField(), "costOfCarryField");
      ArgumentChecker.notNullInjected(getValueFieldName(), "valueFieldName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(TradeExchangeTradedPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(TradeExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(PositionExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(SecurityPriceSeriesFunction.class, getHtsResolutionKey(), getValueFieldName()));
      functions.add(functionConfiguration(SimpleFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SimpleFXFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(ValueGreekSensitivityPnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(MarkToMarketPnLFunction.class, getValueFieldName(), getCostOfCarryField()));
      functions.add(functionConfiguration(HistoricalValuationPnLFunction.class));
      functions.add(functionConfiguration(VolatilityWeightedHistoricalValuationPnLFunction.class));
    }

  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Per currency information.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfiguration;
      private String _discountingCurve;
      private String _surfaceName;

      public void setCurveConfiguration(final String curveConfiguration) {
        _curveConfiguration = curveConfiguration;
      }

      public String getCurveConfiguration() {
        return _curveConfiguration;
      }

      public void setDiscountingCurve(final String discountingCurve) {
        _discountingCurve = discountingCurve;
      }

      public String getDiscountingCurve() {
        return _discountingCurve;
      }

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
        ArgumentChecker.notNullInjected(getDiscountingCurve(), "discountingCurve");
      }

    }

    /**
     * Per currency-pair information.
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
    private String _curveName;
    private String _payCurveName;
    private String _receiveCurveName;
    private String _returnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;

    @Deprecated
    private String _samplingPeriodName = "P2Y";

    private String _start = "-P2Y";
    private String _end = "Now";

    private String _scheduleName = ScheduleCalculatorFactory.DAILY;
    private String _samplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    private String _interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC;
    private String _leftExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    private String _rightExtrapolator = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

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

    public void setCurveName(final String curveName) {
      _curveName = curveName;
    }

    public String getCurveName() {
      return _curveName;
    }

    public void setPayCurveName(final String payCurveName) {
      _payCurveName = payCurveName;
    }

    public String getPayCurveName() {
      return _payCurveName;
    }

    public void setReceiveCurveName(final String receiveCurveName) {
      _receiveCurveName = receiveCurveName;
    }

    public String getReceiveCurveName() {
      return _receiveCurveName;
    }

    public void setReturnCalculatorName(final String returnCalculatorName) {
      _returnCalculatorName = returnCalculatorName;
    }

    public String getReturnCalculatorName() {
      return _returnCalculatorName;
    }

    /**
     *
     * @param samplingPeriodName  the sampling period name
     * @deprecated use start and end instead
     */
    @Deprecated
    public void setSamplingPeriodName(final String samplingPeriodName) {
      _samplingPeriodName = samplingPeriodName;
    }

    /**
     *
     * @return the sampling period name
     * @deprecated  use start and end instead
     */
    @Deprecated
    public String getSamplingPeriodName() {
      return _samplingPeriodName;
    }

    public String getStart() {
      return _start;
    }

    public void setStart(final String start) {
      _start = start;
    }

    public String getEnd() {
      return _end;
    }

    public void setEnd(final String end) {
      _end = end;
    }

    public void setScheduleName(final String scheduleName) {
      _scheduleName = scheduleName;
    }

    public String getScheduleName() {
      return _scheduleName;
    }

    public void setSamplingCalculatorName(final String samplingCalculatorName) {
      _samplingCalculatorName = samplingCalculatorName;
    }

    public String getSamplingCalculatorName() {
      return _samplingCalculatorName;
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
      ArgumentChecker.notNullInjected(getReturnCalculatorName(), "returnCalculatorName");
      ArgumentChecker.notNullInjected(getSamplingPeriodName(), "samplingPeriodName");
      ArgumentChecker.notNullInjected(getStart(), "start");
      ArgumentChecker.notNullInjected(getEnd(), "end");
      ArgumentChecker.notNullInjected(getScheduleName(), "scheduleName");
      ArgumentChecker.notNullInjected(getSamplingCalculatorName(), "samplingCalculatorName");
      ArgumentChecker.notNullInjected(getInterpolator(), "interpolator");
      ArgumentChecker.notNullInjected(getLeftExtrapolator(), "leftExtrapolator");
      ArgumentChecker.notNullInjected(getRightExtrapolator(), "rightExtrapolator");
      super.afterPropertiesSet();
    }

    protected void addBondFutureOptionBlackYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      int i = 0;
      for (final CurrencyInfo e : getPerCurrencyInfo().values()) {
        if (e.getSurfaceName() != null) {
          i++;
        }
      }
      final String[] args = new String[3 + i * 3];
      i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        if (e.getValue().getSurfaceName() != null) {
          args[i++] = e.getKey();
          args[i++] = e.getValue().getCurveConfiguration();
          args[i++] = e.getValue().getSurfaceName();
        }
      }
      functions.add(functionConfiguration(BondFutureOptionBlackYieldCurveNodePnLDefaults.class, args));
    }

    protected void addCreditInstrumetCS01PnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3];
      args[0] = getSamplingPeriodName();
      args[1] = getScheduleName();
      args[2] = getSamplingCalculatorName();
      functions.add(functionConfiguration(CreditInstrumentCS01PnLDefaults.class, args));
    }

    protected void addFXForwardPnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[4 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = getStart();
      args[i++] = getEnd();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getDiscountingCurve();
      }
      functions.add(functionConfiguration(FXForwardPnLDefaults.class, args));
    }

    protected void addFXOptionBlackPnLCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getDiscountingCurve();
      }
      functions.add(functionConfiguration(FXOptionBlackPnLCurveDefaults.class, args));
    }

    protected void addFXOptionBlackPnLSurfaceDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3 + getPerCurrencyPairInfo().size() * 3];
      int i = 0;
      args[i++] = getInterpolator();
      args[i++] = getLeftExtrapolator();
      args[i++] = getRightExtrapolator();
      for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
        args[i++] = e.getKey().getFirst();
        args[i++] = e.getKey().getSecond();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(FXOptionBlackPnLSurfaceDefaults.class, args));
    }

    protected void addInterestRateFutureOptionBlackYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      int i = 0;
      for (final CurrencyInfo e : getPerCurrencyInfo().values()) {
        if (e.getSurfaceName() != null) {
          i++;
        }
      }
      final String[] args = new String[3 + i * 3];
      i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        if (e.getValue().getSurfaceName() != null) {
          args[i++] = e.getKey();
          args[i++] = e.getValue().getCurveConfiguration();
          args[i++] = e.getValue().getSurfaceName();
        }
      }
      functions.add(functionConfiguration(InterestRateFutureOptionBlackYieldCurveNodePnLDefaults.class, args));
    }

    protected void addInterestRateFutureYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(InterestRateFutureYieldCurveNodePnLDefaults.class, args));
    }

    protected void addSwaptionBlackYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      int i = 0;
      for (final CurrencyInfo e : getPerCurrencyInfo().values()) {
        if (e.getSurfaceName() != null) {
          i++;
        }
      }
      final String[] args = new String[3 + i * 3];
      i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        if (e.getValue().getSurfaceName() != null) {
          args[i++] = e.getKey();
          args[i++] = e.getValue().getCurveConfiguration();
          args[i++] = e.getValue().getSurfaceName();
        }
      }
      functions.add(functionConfiguration(SwaptionBlackYieldCurveNodePnLDefaults.class, args));
    }

    protected void addYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(YieldCurveNodePnLDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName(),
          getReturnCalculatorName()));
      functions.add(functionConfiguration(FXOptionBlackPnLDefaults.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName()));
      functions.add(functionConfiguration(PositionPnLDefaults.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName()));
      functions.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName()));
      if (getCurveName() != null) {
        functions.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, getCurveName(), getSamplingPeriodName(), getScheduleName(),
            getSamplingCalculatorName()));
      }
      if ((getPayCurveName() != null) && (getReceiveCurveName() != null)) {
        functions.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, getPayCurveName(), getReceiveCurveName(), getSamplingPeriodName(), getScheduleName(),
            getSamplingCalculatorName()));
      }
      functions.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName(), getReturnCalculatorName()));
      if (!getPerCurrencyInfo().isEmpty()) {
        addBondFutureOptionBlackYieldCurveNodePnLDefaults(functions);
        addFXForwardPnLDefaults(functions);
        addFXOptionBlackPnLCurveDefaults(functions);
        addInterestRateFutureOptionBlackYieldCurveNodePnLDefaults(functions);
        addInterestRateFutureYieldCurveNodePnLDefaults(functions);
        addSwaptionBlackYieldCurveNodePnLDefaults(functions);
        addYieldCurveNodePnLDefaults(functions);
      }
      if (!getPerCurrencyPairInfo().isEmpty()) {
        addFXOptionBlackPnLSurfaceDefaults(functions);
      }
      addCreditInstrumetCS01PnLDefaults(functions);
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(CreditDefaultSwapIndexCS01PnLFunction.class));
    functions.add(functionConfiguration(CreditDefaultSwapOptionCS01PnLFunction.class));
    functions.add(functionConfiguration(CreditInstrumentCS01PnLFunction.class));
    functions.add(functionConfiguration(EquityPnLFunction.class));
    functions.add(functionConfiguration(FXForwardCurrencyExposurePnLFunction.class));
    functions.add(functionConfiguration(FXForwardYieldCurvesPnLFunction.class));
    functions.add(functionConfiguration(FXForwardYieldCurvePnLFunction.class));
    functions.add(functionConfiguration(FXForwardYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(FXOptionBlackDeltaPnLFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaPnLFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(InterestRateFutureYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionPnLFunction.class));
    functions.add(functionConfiguration(SwaptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(YieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, MissingInputsFunction.AGGREGATION_STYLE_FULL));
    functions.add(functionConfiguration(PnLPeriodTranslationFunction.class, ValueRequirementNames.PNL_SERIES));
    functions.add(functionConfiguration(PnLPeriodTranslationFunction.class, ValueRequirementNames.YIELD_CURVE_PNL_SERIES));
    functions.add(functionConfiguration(PnLPeriodTranslationFunction.class, ValueRequirementNames.CURVE_PNL_SERIES));
  }

}
