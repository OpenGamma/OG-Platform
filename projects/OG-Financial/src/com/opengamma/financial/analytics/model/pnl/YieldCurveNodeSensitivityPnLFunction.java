/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class YieldCurveNodeSensitivityPnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private final String _resolutionKey;

  public YieldCurveNodeSensitivityPnLFunction(final String resolutionKey) {
    ArgumentChecker.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final String currency = FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final String forwardCurveName = getPropertyName(constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE));
    final String fundingCurveName = getPropertyName(constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE));
    final String curveCalculationMethodName = getPropertyName(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD));
    final ValueProperties forwardCurveProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethodName)
      .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get();
    final ValueProperties fundingCurveProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethodName)
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
    final Object forwardCurveSensitivitiesObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, position, forwardCurveProperties));
    if (forwardCurveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + forwardCurveName);
    }
    final DoubleLabelledMatrix1D forwardCurveSensitivities = (DoubleLabelledMatrix1D) forwardCurveSensitivitiesObject;
    final Object fundingCurveSensitivitiesObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, position, fundingCurveProperties));
    if (fundingCurveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + fundingCurveName);
    }
    final DoubleLabelledMatrix1D fundingCurveSensitivities = (DoubleLabelledMatrix1D) fundingCurveSensitivitiesObject;
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR));
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION));
    final LocalDate[] schedule = scheduleCalculator.getSchedule(startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded?
    DoubleTimeSeries<?> result = getPnLSeries(forwardCurveSensitivities, historicalSource, startDate, now, schedule, samplingFunction)
      .add(getPnLSeries(fundingCurveSensitivities, historicalSource, startDate, now, schedule, samplingFunction));
    result = result.multiply(position.getQuantity().doubleValue());
    final ValueProperties resultProperties = getResultProperties(desiredValue, currency);
    final ValueSpecification resultSpec = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, resultProperties), getUniqueId());
    return Sets.newHashSet(new ComputedValue(resultSpec, result));
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    if (security instanceof SwapSecurity) {
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_IBOR_IBOR;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(security);
  }
  
  @Override 
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final String currency = FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethodNames == null || curveCalculationMethodNames.isEmpty() || curveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurveNames == null || forwardCurveNames.isEmpty() || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> fundingCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurveNames == null || fundingCurveNames.isEmpty() || fundingCurveNames.size() != 1) {
      return null;
    }
    final String curveCalculationMethodName = getPropertyName(curveCalculationMethodNames);
    final String forwardCurveName = getPropertyName(forwardCurveNames);
    final String fundingCurveName = getPropertyName(fundingCurveNames);
    final ValueProperties forwardSensitivityProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethodName)
      .with(ValuePropertyNames.CURVE, forwardCurveName).get();
    final ValueProperties fundingSensitivityProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethodName)
      .with(ValuePropertyNames.CURVE, fundingCurveName).get();
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, position, forwardSensitivityProperties);
    final ValueRequirement fundingCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, position, fundingSensitivityProperties);
    return Sets.newHashSet(forwardCurveRequirement, fundingCurveRequirement);
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode())
      .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
      .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
      .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .get();
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, properties), getUniqueId()));
  }
  
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }
  
  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String currency) {
    return createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE))
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE))
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD))
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .get();
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
  
  private DoubleTimeSeries<?> getPnLSeries(final DoubleLabelledMatrix1D curveSensitivities, final HistoricalTimeSeriesSource historicalSource, final LocalDate startDate,
      final LocalDate now, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final Object[] labels = curveSensitivities.getLabels();
    final double[] values = curveSensitivities.getValues();    
    for (int i = 0; i < curveSensitivities.size(); i++) {
      final Object idObject = labels[i];
      if (!(idObject instanceof ExternalId)) {
        throw new OpenGammaRuntimeException("Yield curve node sensitivity label was not an external id; should never happen");
      }
      final double sensitivity = values[i];
      final ExternalIdBundle id = ExternalIdBundle.of((ExternalId) idObject);
      final HistoricalTimeSeries dbNodeTimeSeries = historicalSource.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, id, _resolutionKey, startDate, true, now, true);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id + " for " + _resolutionKey + "/" + HistoricalTimeSeriesFields.LAST_PRICE);
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
}
