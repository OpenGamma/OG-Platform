/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.BloombergFXSpotRateIdentifierVisitor;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 *
 */
public class FXForwardYieldCurveNodePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardYieldCurveNodePnLFunction.class);
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveCalculationConfigName = desiredValue.getConstraint(FXForwardFunction.PAY_CURVE_CALC_CONFIG);
    final String receiveCurveCalculationConfigName = desiredValue.getConstraint(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final Period samplingPeriod = getSamplingPeriod(desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig payCurveCalculationConfig = curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
    final MultiCurveCalculationConfig receiveCurveCalculationConfig = curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
    final ValueRequirement payYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        payCurrency.getCode(), payCurveName, security);
    final ValueRequirement receiveYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        receiveCurrency.getCode(), receiveCurveName, security);
    final Object payYCNSObject = inputs.getValue(payYCNSRequirement);
    if (payYCNSObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve node sensitivities; " + payYCNSRequirement);
    }
    final Object receiveYCNSObject = inputs.getValue(receiveYCNSRequirement);
    if (receiveYCNSObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve node sensitivities; " + receiveYCNSRequirement);
    }
    final DoubleLabelledMatrix1D payYCNS = (DoubleLabelledMatrix1D) payYCNSObject;
    final DoubleLabelledMatrix1D receiveYCNS = (DoubleLabelledMatrix1D) receiveYCNSObject;
    final String samplingPeriodName = samplingPeriod.toString();
    final ValueRequirement payYCHTSRequirement = getYCHTSRequirement(payCurrency, payCurveName, samplingPeriodName);
    final ValueRequirement receiveYCHTSRequirement = getYCHTSRequirement(receiveCurrency, receiveCurveName, samplingPeriodName);
    final Object payYCHTSObject = inputs.getValue(payYCHTSRequirement);
    if (payYCHTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve historical time series; " + payYCHTSRequirement);
    }
    final Object receiveYCHTSObject = inputs.getValue(receiveYCHTSRequirement);
    if (receiveYCHTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve historical time series; " + receiveYCHTSRequirement);
    }
    final HistoricalTimeSeriesBundle payYCHTS = (HistoricalTimeSeriesBundle) payYCHTSObject;
    final HistoricalTimeSeriesBundle receiveYCHTS = (HistoricalTimeSeriesBundle) receiveYCHTSObject;
    DoubleTimeSeries<?> payResult = null;
    payResult = getPnLForCurve(inputs, position, payCurrency, payCurveName, samplingFunction, schedule, payResult, payCurveCalculationConfig, payYCNS, payYCHTS);
    DoubleTimeSeries<?> receiveResult = null;
    receiveResult = getPnLForCurve(inputs, position, receiveCurrency, receiveCurveName, samplingFunction, schedule, receiveResult, receiveCurveCalculationConfig,
        receiveYCNS, receiveYCHTS);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final Currency currencyBase = currencyPair.getBase();
    final Object fxSpotTSObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    if (fxSpotTSObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot FX time series");
    }
    final DoubleTimeSeries<?> fxSpotTS = ((HistoricalTimeSeries) fxSpotTSObject).getTimeSeries();
    DoubleTimeSeries<?> result;
    if (payCurrency.equals(currencyBase.getCode())) {
      result = payResult;
      result = result.add(receiveResult.multiply(fxSpotTS));
    } else {
      result = receiveResult;
      result = result.add(payResult.multiply(fxSpotTS));
    }
    final ValueProperties resultProperties = getResultProperties(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        currencyBase.getCode(), samplingPeriodName, scheduleCalculatorName, samplingFunctionName);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpec, payResult));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Currency currencyBase = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency).getBase();
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(FXForwardFunction.PAY_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)
        .get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationConfigNames = constraints.getValues(FXForwardFunction.PAY_CURVE_CALC_CONFIG);
    if (payCurveCalculationConfigNames == null || payCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigNames = constraints.getValues(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    if (receiveCurveCalculationConfigNames == null || receiveCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final String payCurveCalculationConfigName = Iterables.getOnlyElement(payCurveCalculationConfigNames);
    final String receiveCurveCalculationConfigName = Iterables.getOnlyElement(receiveCurveCalculationConfigNames);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig payCurveCalculationConfig = curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
    if (payCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + payCurveCalculationConfigName);
      return null;
    }
    final MultiCurveCalculationConfig receiveCurveCalculationConfig = curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
    if (receiveCurveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + receiveCurveCalculationConfigName);
      return null;
    }
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final String payCurveName = Iterables.getOnlyElement(payCurveNames);
    final String receiveCurveName = Iterables.getOnlyElement(receiveCurveNames);
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final String payCurrencyName = payCurrency.getCode();
    final String receiveCurrencyName = receiveCurrency.getCode();
    final ValueRequirement payYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        payCurrencyName, payCurveName, security);
    final ValueRequirement receiveYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        receiveCurrencyName, receiveCurveName, security);
    final String samplingPeriod = Iterables.getOnlyElement(samplingPeriods);
    final ValueRequirement payYCHTSRequirement = getYCHTSRequirement(payCurrency, payCurveName, samplingPeriod);
    final ValueRequirement receiveYCHTSRequirement = getYCHTSRequirement(receiveCurrency, receiveCurveName, samplingPeriod);
    final HistoricalTimeSeriesResolver historicalTimeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ValueRequirement fxSpotRequirement = getFXSpotRequirement(historicalTimeSeriesResolver, security, new BloombergFXSpotRateIdentifierVisitor(currencyPairs), samplingPeriod);
    if (fxSpotRequirement == null) {
      s_logger.error("Could not get time series for FX spot series {} / {}", payCurrencyName, receiveCurrencyName);
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(payYCNSRequirement);
    requirements.add(payYCHTSRequirement);
    requirements.add(receiveYCNSRequirement);
    requirements.add(receiveYCHTSRequirement);
    requirements.add(fxSpotRequirement);
    if (!payCurveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(payCurrency, payCurveName));
    }
    if (!receiveCurveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(receiveCurrency, receiveCurveName));
    }
    return requirements;
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String yieldCurveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, currency, properties);
  }

  private ValueRequirement getFXSpotRequirement(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver, final FinancialSecurity security,
      final FinancialSecurityVisitor<ExternalId> visitor, final String samplingPeriods) {
    final HistoricalTimeSeriesResolutionResult timeSeries = historicalTimeSeriesResolver.resolve(
        ExternalIdBundle.of(security.accept(visitor)), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries,
        MarketDataRequirementNames.MARKET_VALUE, DateConstraint.VALUATION_TIME.minus(samplingPeriods), true, DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getYCHTSRequirement(final Currency currency, final String yieldCurveName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createYCHTSRequirement(currency, yieldCurveName, MarketDataRequirementNames.MARKET_VALUE, null, DateConstraint.VALUATION_TIME.minus(samplingPeriod), true,
        DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getYCNSRequirement(final String payCurveName, final String payCurveCalculationConfigName, final String receiveCurveName,
      final String receiveCurveCalculationConfigName, final String currencyName, final String curveName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(FXForwardFunction.PAY_CURVE_CALC_CONFIG, payCurveCalculationConfigName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, security, properties);
  }

  private DoubleTimeSeries<?> getPnLForCurve(final FunctionInputs inputs, final Position position, final Currency currency, final String curveName,
      final TimeSeriesSamplingFunction samplingFunction, final LocalDate[] schedule, DoubleTimeSeries<?> result,
      final MultiCurveCalculationConfig curveCalculationConfig, final DoubleLabelledMatrix1D ycns, final HistoricalTimeSeriesBundle ychts) {
    final DoubleTimeSeries<?> pnLSeries;
    if (curveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      pnLSeries = getPnLSeries(ycns, ychts, schedule, samplingFunction);
    } else {
      final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
      final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
      if (curveSpecObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve specification; " + curveSpecRequirement);
      }
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
      pnLSeries = getPnLSeries(curveSpec, ycns, ychts, schedule, samplingFunction);
    }
    if (result == null) {
      result = pnLSeries;
    } else {
      result = result.add(pnLSeries);
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Could not get any values for security " + position.getSecurity());
    }
    result = result.multiply(position.getQuantity().doubleValue());
    return result;
  }

  private DoubleTimeSeries<?> getPnLSeries(final InterpolatedYieldCurveSpecificationWithSecurities spec, final DoubleLabelledMatrix1D curveSensitivities,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final int n = curveSensitivities.size();
    final Object[] labels = curveSensitivities.getLabels();
    final List<Object> labelsList = Arrays.asList(labels);
    final double[] values = curveSensitivities.getValues();
    final Set<FixedIncomeStripWithSecurity> strips = spec.getStrips();
    final List<StripInstrumentType> stripList = new ArrayList<StripInstrumentType>(n);
    for (final FixedIncomeStripWithSecurity strip : strips) {
      final int index = labelsList.indexOf(strip.getSecurityIdentifier());
      if (index < 0) {
        throw new OpenGammaRuntimeException("Could not get index for " + strip);
      }
      stripList.add(index, strip.getInstrumentType());
    }
    for (int i = 0; i < n; i++) {
      final ExternalId id = (ExternalId) labels[i];
      double sensitivity = values[i];
      if (stripList.get(i) == StripInstrumentType.FUTURE) {
        // TODO Temporary fix as sensitivity is to rate, but historical time series is to price (= 1 - rate)
        sensitivity *= -1;
      }
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id);
      }
      if (dbNodeTimeSeries.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Time series " + id + " is empty");
      }
      DoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(sensitivity);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(sensitivity));
      }
    }
    return pnlSeries;
  }

  private DoubleTimeSeries<?> getPnLSeries(final DoubleLabelledMatrix1D curveSensitivities, final HistoricalTimeSeriesBundle timeSeriesBundle, final LocalDate[] schedule,
      final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final Object[] labels = curveSensitivities.getLabels();
    final double[] values = curveSensitivities.getValues();
    for (int i = 0; i < labels.length; i++) {
      final ExternalId id = (ExternalId) labels[i];
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id);
      }
      DoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(values[i]);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(values[i]));
      }
    }
    return pnlSeries;
  }

  private Period getSamplingPeriod(final String samplingPeriodName) {
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  private ValueProperties getResultProperties(final String payCurveName, final String payCurveCalcConfig, final String receiveCurveName, final String receiveCurveCalcConfig,
      final String currencyBase, final String samplingPeriod, final String scheduleCalculator, final String samplingFunction) {
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(FXForwardFunction.PAY_CURVE_CALC_CONFIG, payCurveCalcConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalcConfig)
        .with(ValuePropertyNames.CURRENCY, currencyBase)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)
        .get();
  }
}
