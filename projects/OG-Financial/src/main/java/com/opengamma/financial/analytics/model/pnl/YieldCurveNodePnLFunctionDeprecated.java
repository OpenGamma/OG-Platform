/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see YieldCurveNodePnLFunction
 */
@Deprecated
public class YieldCurveNodePnLFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  /** Property name of the contribution to the P&L (e.g. yield curve, FX rate) */
  public static final String PROPERTY_PNL_CONTRIBUTIONS = "PnLContribution";
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final String currencyString = currency.getCode();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final String forwardCurveName = getPropertyName(constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE));
    final String fundingCurveName = getPropertyName(constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE));
    final String curveCalculationMethodName = getPropertyName(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD));
    final ValueProperties.Builder forwardCurveSpecProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, forwardCurveName);
    final ValueRequirement forwardCurveSpecRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency),
        forwardCurveSpecProperties.get());
    final Object forwardCurveSpecObject = inputs.getValue(forwardCurveSpecRequirement);
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveSpecRequirement);
    }
    final ValueProperties.Builder fundingCurveSpecProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, fundingCurveName);
    final ValueRequirement fundingCurveSpecRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency),
        fundingCurveSpecProperties.get());
    final Object fundingCurveSpecObject = inputs.getValue(fundingCurveSpecRequirement);
    if (fundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fundingCurveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final ValueProperties forwardCurveProperties = getSensitivityProperties(currencyString, forwardCurveName, fundingCurveName, curveCalculationMethodName, forwardCurveName);
    final ValueProperties fundingCurveProperties = getSensitivityProperties(currencyString, forwardCurveName, fundingCurveName, curveCalculationMethodName, fundingCurveName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Object forwardCurveSensitivitiesObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, targetSpec, forwardCurveProperties));
    if (forwardCurveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + forwardCurveName);
    }
    final DoubleLabelledMatrix1D forwardCurveSensitivities = (DoubleLabelledMatrix1D) forwardCurveSensitivitiesObject;
    final Object fundingCurveSensitivitiesObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, targetSpec, fundingCurveProperties));
    if (fundingCurveSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get sensitivities for " + fundingCurveName);
    }
    final DoubleLabelledMatrix1D fundingCurveSensitivities = (DoubleLabelledMatrix1D) fundingCurveSensitivitiesObject;
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR));
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION));
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    final DoubleTimeSeries<?> result = getPnLSeries(forwardCurveSpec, forwardCurveSensitivities, historicalSource, startDate, now, schedule, samplingFunction)
        .add(getPnLSeries(fundingCurveSpec, fundingCurveSensitivities, historicalSource, startDate, now, schedule, samplingFunction));
    final ValueProperties resultProperties = getResultProperties(desiredValue, currencyString);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, targetSpec, resultProperties);
    return Sets.newHashSet(new ComputedValue(resultSpec, result));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      try {
        final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity((SwapSecurity) security);
        return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_IBOR_IBOR;
      } catch (final OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) security);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final String currencyString = currency.getCode();
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
    final ValueProperties forwardSensitivityProperties = getSensitivityProperties(currencyString, forwardCurveName, fundingCurveName, curveCalculationMethodName, forwardCurveName);
    final ValueProperties fundingSensitivityProperties = getSensitivityProperties(currencyString, forwardCurveName, fundingCurveName, curveCalculationMethodName, fundingCurveName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, targetSpec, forwardSensitivityProperties);
    final ValueRequirement fundingCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, targetSpec, fundingSensitivityProperties);
    final ValueProperties.Builder forwardCurveSpecProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, forwardCurveName);
    final ComputationTargetSpecification curveSpec = ComputationTargetSpecification.of(currency);
    final ValueRequirement forwardCurveSpecRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, curveSpec, forwardCurveSpecProperties.get());
    final ValueProperties.Builder fundingCurveSpecProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, fundingCurveName);
    final ValueRequirement fundingCurveSpecRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, curveSpec, fundingCurveSpecProperties.get());
    return Sets.newHashSet(forwardCurveRequirement, fundingCurveRequirement, forwardCurveSpecRequirement, fundingCurveSpecRequirement);
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
        .with(PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String currency) {
    return createValueProperties().with(ValuePropertyNames.CURRENCY, currency).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE))
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE))
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD))
        .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
        .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
        .with(PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES).get();
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
      final HistoricalTimeSeriesSource historicalSource, final LocalDate startDate, final LocalDate now, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
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
      final Object idObject = labels[i];
      if (!(idObject instanceof ExternalId)) {
        throw new OpenGammaRuntimeException("Yield curve node sensitivity label was not an external id; should never happen");
      }
      double sensitivity = values[i];
      if (stripList.get(i) == StripInstrumentType.FUTURE) {
        // TODO Temporary fix as sensitivity is to rate, but historical time series is to price (= 1 - rate)
        sensitivity *= -1;
      }
      final ExternalIdBundle id = ExternalIdBundle.of((ExternalId) idObject);
      final HistoricalTimeSeries dbNodeTimeSeries = historicalSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, startDate, true, now, true);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id + " using the field " + MarketDataRequirementNames.MARKET_VALUE);
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

  private ValueProperties getSensitivityProperties(final String currencyString, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethodName,
      final String curveName) {
    return ValueProperties.builder().with(ValuePropertyNames.CURRENCY, currencyString).with(ValuePropertyNames.CURVE_CURRENCY, currencyString)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethodName).with(ValuePropertyNames.CURVE, curveName).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
  }
}
