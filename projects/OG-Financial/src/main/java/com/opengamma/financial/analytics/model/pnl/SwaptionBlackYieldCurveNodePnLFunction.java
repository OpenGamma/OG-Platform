/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SwaptionBlackYieldCurveNodePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBlackYieldCurveNodePnLFunction.class);
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final String currencyString = currency.getCode();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final String surfaceName = getPropertyName(constraints.getValues(ValuePropertyNames.SURFACE));
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Set<String> yieldCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR));
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION));
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    DoubleTimeSeries<?> result = null;
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    for (final String yieldCurveName : yieldCurveNames) {
      final ValueRequirement ycnsRequirement = getYCNSRequirement(currencyString, curveCalculationConfigName, yieldCurveName, surfaceName, target);
      final DoubleLabelledMatrix1D ycns = (DoubleLabelledMatrix1D) inputs.getValue(ycnsRequirement);
      final HistoricalTimeSeriesBundle ychts = (HistoricalTimeSeriesBundle) inputs.getValue(getYCHTSRequirement(currency, yieldCurveName, samplingPeriod.toString()));
      final DoubleTimeSeries<?> pnLSeries;
      if (curveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
        pnLSeries = getPnLSeries(ycns, ychts, schedule, samplingFunction);
      } else {
        final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, yieldCurveName);
        final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) inputs.getValue(curveSpecRequirement);
        pnLSeries = getPnLSeries(curveSpec, ycns, ychts, schedule, samplingFunction);
      }
      if (result == null) {
        result = pnLSeries;
      } else {
        result = result.add(result);
      }
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Could not get any values for security " + position.getSecurity());
    }
    result = result.multiply(position.getQuantity().doubleValue());
    final ValueProperties resultProperties = getResultProperties(desiredValue, currencyString);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpec, result));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof SwaptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final String currencyString = currency.getCode();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final String samplingPeriod = samplingPeriods.iterator().next();
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
    }
    final String surfaceName = getPropertyName(surfaceNames);
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (final String curveName : curveCalculationConfig.getYieldCurveNames()) {
      requirements.add(getCurveSpecRequirement(currency, curveName));
      requirements.add(getYCNSRequirement(currencyString, curveCalculationConfigName, curveName, surfaceName, target));
      requirements.add(getYCHTSRequirement(currency, curveName, samplingPeriod));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode()).withAny(ValuePropertyNames.SURFACE).withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).withAny(ValuePropertyNames.SAMPLING_PERIOD).withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION).with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<String> curveNames = new HashSet<>();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      if (entry.getKey().getValueName().equals(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)) {
        curveNames.add(entry.getValue().getConstraint(ValuePropertyNames.CURVE));
      }
    }
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode()).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURVE, curveNames).withAny(ValuePropertyNames.SURFACE).withAny(ValuePropertyNames.SAMPLING_PERIOD).withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION).with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String currency) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE))
        .with(ValuePropertyNames.SURFACE, desiredValue.getConstraint(ValuePropertyNames.SURFACE))
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG))
        .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
        .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
  }

  private String getPropertyName(final Set<String> propertyName) {
    if (propertyName == null || propertyName.isEmpty() || propertyName.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique property name: " + propertyName);
    }
    return propertyName.iterator().next();
  }

  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    final String samplingPeriodName = getPropertyName(samplingPeriodNames);
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final Set<String> scheduleCalculatorNames) {
    final String scheduleCalculatorName = getPropertyName(scheduleCalculatorNames);
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final Set<String> samplingFunctionNames) {
    final String samplingFunctionName = getPropertyName(samplingFunctionNames);
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  private DoubleTimeSeries<?> getPnLSeries(final InterpolatedYieldCurveSpecificationWithSecurities spec, final DoubleLabelledMatrix1D curveSensitivities,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final int n = curveSensitivities.size();
    final Object[] labels = curveSensitivities.getLabels();
    final List<Object> labelsList = Arrays.asList(labels);
    final double[] values = curveSensitivities.getValues();
    final SortedSet<FixedIncomeStripWithSecurity> strips = (SortedSet<FixedIncomeStripWithSecurity>) spec.getStrips();
    final FixedIncomeStripWithSecurity[] stripsArray = strips.toArray(new FixedIncomeStripWithSecurity[] {});
    final List<StripInstrumentType> stripList = new ArrayList<StripInstrumentType>(n);
    int stripCount = 0;
    for (final FixedIncomeStripWithSecurity strip : strips) {
      final int index = stripCount++; //labelsList.indexOf(strip.getSecurityIdentifier());
      if (index < 0) {
        throw new OpenGammaRuntimeException("Could not get index for " + strip);
      }
      stripList.add(index, strip.getInstrumentType());
    }
    for (int i = 0; i < n; i++) {
      final ExternalId id = stripsArray[i].getSecurityIdentifier();
      final double sensitivity = values[i];
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (dbNodeTimeSeries.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Time series " + id + " is empty");
      }
      DateDoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
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
      DateDoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(values[i]);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(values[i]));
      }
    }
    return pnlSeries;
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getYCNSRequirement(final String currencyString, final String curveCalculationConfig, final String curveName, final String surfaceName,
      final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURRENCY, currencyString).with(ValuePropertyNames.CURVE_CURRENCY, currencyString).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.SURFACE, surfaceName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
  }

  private ValueRequirement getYCHTSRequirement(final Currency currency, final String yieldCurveName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createYCHTSRequirement(currency, yieldCurveName, MarketDataRequirementNames.MARKET_VALUE, null,
        DateConstraint.VALUATION_TIME.minus(samplingPeriod), true, DateConstraint.VALUATION_TIME, true);
  }

}
