/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * Calculates return series for the market instruments at the nodal points of a yield curve.
 */
public class YieldCurveNodeReturnSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveNodeReturnSeriesFunction.class);
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    // NOTE jonathan 2013-04-23 -- should be ComputationTargetType.NULL
    return ComputationTargetType.CURRENCY;
  }

  protected ValueProperties getResultProperties(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION).withAny(ValuePropertyNames.SCHEDULE_CALCULATOR).withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(ValuePropertyNames.CURRENCY, ((Currency) target.getValue()).getCode()).with(ValuePropertyNames.TRANSFORMATION_METHOD, "None").get();
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target);
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Currency currency = (Currency) target.getValue();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curveNames);
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);

    final String ychtsStart = getYCHTSStart(constraints);
    if (ychtsStart == null) {
      return null;
    }
    final DateConstraint start = DateConstraint.parse(ychtsStart);
    final String returnSeriesEnd = getReturnSeriesEnd(constraints);
    if (returnSeriesEnd == null) {
      return null;
    }
    final DateConstraint end = DateConstraint.parse(returnSeriesEnd);
    if (start == null || end == null) {
      return null;
    }

    final Set<String> includeStarts = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if (includeStarts != null && includeStarts.size() != 1) {
      return null;
    }
    final boolean includeStart = includeStarts == null ? true : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeStarts));
    final Set<String> includeEnds = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if (includeEnds != null && includeEnds.size() != 1) {
      return null;
    }
    final boolean includeEnd = includeEnds == null ? false : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeEnds));
    final Set<String> samplingMethod = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingMethod == null || samplingMethod.size() != 1) {
      return null;
    }
    final Set<String> scheduleMethod = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleMethod == null || scheduleMethod.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(HistoricalTimeSeriesFunctionUtils.createYCHTSRequirement(currency, curveName, MarketDataRequirementNames.MARKET_VALUE, null, start, includeStart, end, includeEnd));

    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not get curve calculation config called " + curveCalculationConfigName);
      return null;
    }
    if (FXImpliedYieldCurveFunction.FX_IMPLIED.equals(curveCalculationConfig.getCalculationMethod())) {
      final Currency impliedCcy = ComputationTargetType.CURRENCY.resolve(curveCalculationConfig.getTarget().getUniqueId());
      final String baseCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfig.getExogenousConfigData().entrySet()).getKey();
      final MultiCurveCalculationConfig baseCurveCalculationConfig = _curveCalculationConfigSource.getConfig(baseCalculationConfigName);
      final Currency baseCcy = ComputationTargetType.CURRENCY.resolve(baseCurveCalculationConfig.getTarget().getUniqueId());
      requirements.add(getFXForwardCurveDefinitionRequirement(UnorderedCurrencyPair.of(impliedCcy, baseCcy), curveName));
    } else {
      requirements.add(getCurveSpecRequirement(currency, curveName));
    }
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final LocalDate tsStart = DateConstraint.evaluate(executionContext, getYCHTSStart(desiredValue.getConstraints()));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(executionContext, getReturnSeriesStart(desiredValue.getConstraints()));
    if (tsStart.isAfter(returnSeriesStart)) {
      throw new OpenGammaRuntimeException("Return series start date cannot be before time-series start date");
    }
    final LocalDate returnSeriesEnd = DateConstraint.evaluate(executionContext, getReturnSeriesEnd(desiredValue.getConstraints()));
    final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);

    //REVIEW emcleod should "fromEnd" be hard-coded?
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(tsStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR);

    final ComputedValue bundleValue = inputs.getComputedValue(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES);
    final HistoricalTimeSeriesBundle bundle = (HistoricalTimeSeriesBundle) bundleValue.getValue();
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(bundleValue.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    final FXForwardCurveDefinition fxForwardCurveDefinition = (FXForwardCurveDefinition) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION);

    Tenor[] tenors;
    boolean[] sensitivityToRate;
    if (curveSpec != null) {
      final Set<FixedIncomeStripWithSecurity> strips = curveSpec.getStrips();
      final int n = strips.size();
      tenors = new Tenor[n];
      sensitivityToRate = new boolean[n];
      int i = 0;
      for (final FixedIncomeStripWithSecurity strip : strips) {
        tenors[i] = strip.getTenor();
        // TODO Temporary fix as sensitivity is to rate, but historical time series is to price (= 1 - rate)
        sensitivityToRate[i] = strip.getInstrumentType() == StripInstrumentType.FUTURE;
        i++;
      }
    } else if (fxForwardCurveDefinition != null) {
      tenors = fxForwardCurveDefinition.getTenorsArray();
      sensitivityToRate = new boolean[tenors.length];
    } else {
      throw new OpenGammaRuntimeException("Yield curve specification and FX forward curve definition both missing. Expected one.");
    }

    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeriesVector = getReturnSeriesVector(bundle, tenors, sensitivityToRate, schedule, samplingFunction, returnSeriesStart,
        includeStart, desiredValue);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(resultSpec, returnSeriesVector));
  }

  protected String getYCHTSStart(final ValueProperties constraints) {
    return getReturnSeriesStart(constraints);
  }

  protected String getReturnSeriesStart(final ValueProperties constraints) {
    final Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(startDates);
  }

  protected String getReturnSeriesEnd(final ValueProperties constraints) {
    final Set<String> endDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (endDates == null || endDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(endDates);
  }

  protected LocalDateDoubleTimeSeries getReturnSeries(final LocalDateDoubleTimeSeries ts, final ValueRequirement desiredValue) {
    return (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(ts);
  }

  private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getReturnSeriesVector(final HistoricalTimeSeriesBundle timeSeriesBundle, final Tenor[] tenors, final boolean[] sensitivityToRates,
      final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction, final LocalDate startDate, final boolean includeStart, final ValueRequirement desiredValue) {
    final LocalDateDoubleTimeSeries[] returnSeriesArray = new LocalDateDoubleTimeSeries[tenors.length];
    if (tenors.length != timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE)) {
      throw new OpenGammaRuntimeException("Expected " + tenors.length + " nodal market data time-series but have " + timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE));
    }
    final Iterator<HistoricalTimeSeries> tsIterator = timeSeriesBundle.iterator(MarketDataRequirementNames.MARKET_VALUE);
    if (tsIterator == null) {
      throw new OpenGammaRuntimeException("No nodal market data time-series available");
    }
    for (int t = 0; t < tenors.length; t++) {
      final Tenor tenor = tenors[t];
      final HistoricalTimeSeries dbNodeTimeSeries = tsIterator.next();
      final boolean sensitivityToRate = sensitivityToRates[t];
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("No time-series for strip with tenor " + tenor);
      }
      final LocalDateDoubleTimeSeries nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      LocalDateDoubleTimeSeries returnSeries = getReturnSeries(nodeTimeSeries, desiredValue);
      if (sensitivityToRate) {
        returnSeries = returnSeries.multiply(-1);
      }

      // Clip the time-series to the range originally asked for
      returnSeries = returnSeries.subSeries(startDate, includeStart, returnSeries.getLatestTime(), true);

      returnSeriesArray[t] = returnSeries;
    }
    return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenors, returnSeriesArray);
  }

  //-------------------------------------------------------------------------
  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String yieldCurveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, yieldCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private ValueRequirement getFXForwardCurveDefinitionRequirement(final UnorderedCurrencyPair currencies, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies), properties);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

}
