/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_FUNCTION;
import static com.opengamma.engine.value.ValuePropertyNames.SCHEDULE_CALCULATOR;
import static com.opengamma.engine.value.ValuePropertyNames.TRANSFORMATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_PNL_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.FORWARD_POINTS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;


/**
 *
 */
public class FXForwardPointsFCNSPnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Position position = target.getPosition();
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final DoubleLabelledMatrix1D sensitivities = ((DoubleLabelledMatrix1D) inputs.getValue(FX_FORWARD_POINTS_NODE_SENSITIVITIES));
    final String currency = inputs.getComputedValue(FX_FORWARD_POINTS_NODE_SENSITIVITIES).getSpecification().getProperty(CURRENCY);
    final double[] fcns = sensitivities.getValues();
    final Object[] labels = sensitivities.getLabels();
    final CurveSpecification curveSpec = (CurveSpecification) inputs.getValue(CURVE_SPECIFICATION);
    final int n = fcns.length;
    if (n != curveSpec.getNodes().size()) {
      throw new OpenGammaRuntimeException("Do not have a sensitivity for each node");
    }
    final Tenor[] tenors = new Tenor[n];
    final LocalDateDoubleTimeSeries[] returnSeries = new LocalDateDoubleTimeSeries[n];
    final HistoricalTimeSeriesBundle tsBundle = (HistoricalTimeSeriesBundle) inputs.getValue(CURVE_HISTORICAL_TIME_SERIES);
    final Iterator<CurveNodeWithIdentifier> iterator = curveSpec.getNodes().iterator();
    for (int i = 0; i < n; i++) {
      final double sensitivity = fcns[i];
      final CurveNodeWithIdentifier curveNode = iterator.next();
      final HistoricalTimeSeries ts = tsBundle.get(curveNode.getDataField(), curveNode.getIdentifier());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get time series for id " + curveNode.getIdentifier() + " and data field " + curveNode.getDataField());
      }
      LocalDateDoubleTimeSeries pnlSeries;
      if (curveNode instanceof PointsCurveNodeWithIdentifier) {
        final PointsCurveNodeWithIdentifier pointsCurveNode = (PointsCurveNodeWithIdentifier) curveNode;
        final HistoricalTimeSeries underlyingSeries = tsBundle.get(pointsCurveNode.getUnderlyingDataField(), pointsCurveNode.getUnderlyingIdentifier());
        if (underlyingSeries == null) {
          throw new OpenGammaRuntimeException("Could not get time series for id " + pointsCurveNode.getUnderlyingIdentifier() + " and data field " + pointsCurveNode.getUnderlyingDataField());
        }
        pnlSeries = getReturnSeries(ts.getTimeSeries().add(underlyingSeries.getTimeSeries()), desiredValue, executionContext);
      } else {
        pnlSeries = getReturnSeries(ts.getTimeSeries(), desiredValue, executionContext);
      }
      tenors[i] = curveNode.getCurveNode().getResolvedMaturity();
      returnSeries[i] = pnlSeries.multiply(sensitivity * position.getQuantity().doubleValue());
    }
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix = new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenors, labels, returnSeries);
    final ValueProperties properties = desiredValue.getConstraints().copy()
        .withoutAny(CURRENCY)
        .with(CURRENCY, currency)
        .get();
    final ValueSpecification spec = new ValueSpecification(CURVE_PNL_SERIES, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, matrix));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity ||
        security instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
          .withAny(START_DATE_PROPERTY)
          .withAny(END_DATE_PROPERTY)
          .withAny(INCLUDE_START_PROPERTY)
          .withAny(INCLUDE_END_PROPERTY)
          .with(TRANSFORMATION_METHOD, "None")
          .withAny(SCHEDULE_CALCULATOR)
          .withAny(SAMPLING_FUNCTION)
          .withAny(CURVE_EXPOSURES)
          .withAny(FORWARD_CURVE_NAME)
          .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
          .with(PROPERTY_PNL_CONTRIBUTIONS, FX_FORWARD_POINTS_NODE_SENSITIVITIES)
          .withAny(CURRENCY)
          .get();
    return Collections.singleton(new ValueSpecification(CURVE_PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
    if (curveExposureConfigs == null || curveExposureConfigs.size() != 1) {
      return null;
    }
    final Set<String> fxForwardCurveNames = constraints.getValues(FORWARD_CURVE_NAME);
    if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> includeStarts = constraints.getValues(INCLUDE_START_PROPERTY);
    if (includeStarts != null && includeStarts.size() != 1) {
      return null;
    }
    final Set<String> includeEnds = constraints.getValues(INCLUDE_END_PROPERTY);
    if (includeEnds != null && includeEnds.size() != 1) {
      return null;
    }
    final Set<String> startDates = constraints.getValues(START_DATE_PROPERTY);
    if (startDates != null && startDates.size() != 1) {
      return null;
    }
    final Set<String> endDates = constraints.getValues(END_DATE_PROPERTY);
    if (endDates != null && endDates.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctions = constraints.getValues(SAMPLING_FUNCTION);
    if (samplingFunctions == null || samplingFunctions.size() != 1) {
      return null;
    }
    final Set<String> scheduleMethods = constraints.getValues(SCHEDULE_CALCULATOR);
    if (scheduleMethods == null || scheduleMethods.size() != 1) {
      return null;
    }
    final String curveExposureConfig = Iterables.getOnlyElement(curveExposureConfigs);
    final String fxForwardCurveName = Iterables.getOnlyElement(fxForwardCurveNames);
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_EXPOSURES, curveExposureConfig)
        .with(FORWARD_CURVE_NAME, fxForwardCurveName)
        .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
        .withAny(CURRENCY)
        .get();
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties tsProperties = ValueProperties
        .with(INCLUDE_START_PROPERTY, includeStarts)
        .with(INCLUDE_END_PROPERTY, includeEnds)
        .with(START_DATE_PROPERTY, startDates)
        .with(END_DATE_PROPERTY, endDates)
        .with(CURVE, fxForwardCurveName)
        .get();
    final ValueProperties curveProperties = ValueProperties
        .with(CURVE, fxForwardCurveName)
        .get();
    final UnorderedCurrencyPair ccyPair = UnorderedCurrencyPair.of(payCurrency, receiveCurrency);
    final Trade trade = Iterables.getOnlyElement(target.getPosition().getTrades());
    requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
    requirements.add(ConventionBasedFXRateFunction.getSpotRateRequirement(ccyPair));
    requirements.add(new ValueRequirement(FX_FORWARD_POINTS_NODE_SENSITIVITIES, ComputationTargetSpecification.of(trade), properties));
    requirements.add(new ValueRequirement(CURVE_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, tsProperties));
    requirements.add(new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, ValueProperties.none()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String startDate = null;
    String endDate = null;
    String includeStart = null;
    String includeEnd = null;
    String curveExposures = null;
    String curveName = null;
    String currency = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      final String valueName = key.getValueName();
      if (valueName.equals(FX_FORWARD_POINTS_NODE_SENSITIVITIES)) {
        currency = key.getProperty(CURRENCY);
        curveExposures = key.getProperty(CURVE_EXPOSURES);
        curveName = key.getProperty(FORWARD_CURVE_NAME);
      } else if (valueName.equals(CURVE_HISTORICAL_TIME_SERIES)) {
        startDate = key.getProperty(START_DATE_PROPERTY);
        endDate = key.getProperty(END_DATE_PROPERTY);
        includeStart = key.getProperty(INCLUDE_START_PROPERTY);
        includeEnd = key.getProperty(INCLUDE_END_PROPERTY);
      }
    }
    if (currency == null || startDate == null) {
      return null;
    }
    final ValueProperties properties = createValueProperties()
          .with(START_DATE_PROPERTY, startDate)
          .with(END_DATE_PROPERTY, endDate)
          .with(INCLUDE_START_PROPERTY, includeStart)
          .with(INCLUDE_END_PROPERTY, includeEnd)
          .with(TRANSFORMATION_METHOD, "None")
          .withAny(SCHEDULE_CALCULATOR)
          .withAny(SAMPLING_FUNCTION)
          .with(CURVE_EXPOSURES, curveExposures)
          .with(FORWARD_CURVE_NAME, curveName)
          .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
          .with(PROPERTY_PNL_CONTRIBUTIONS, FX_FORWARD_POINTS_NODE_SENSITIVITIES)
          .with(CURRENCY, currency)
          .get();
    return Collections.singleton(new ValueSpecification(CURVE_PNL_SERIES, target.toSpecification(), properties));
  }

  private static LocalDateDoubleTimeSeries getReturnSeries(final LocalDateDoubleTimeSeries ts, final ValueRequirement desiredValue,
      final FunctionExecutionContext context) {
    final boolean includeStart = Boolean.parseBoolean(desiredValue.getConstraint(INCLUDE_START_PROPERTY));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(context, desiredValue.getConstraint(START_DATE_PROPERTY));
    final LocalDate returnSeriesEnd = DateConstraint.evaluate(context, desiredValue.getConstraint(END_DATE_PROPERTY));
    final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final Schedule scheduleCalculator = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(returnSeriesStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR);
    final LocalDateDoubleTimeSeries sampledTimeSeries = samplingFunction.getSampledTimeSeries(ts, dates);
    final LocalDateDoubleTimeSeries returnSeries = (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(sampledTimeSeries);
    // Clip the time-series to the range originally asked for
    return returnSeries.subSeries(returnSeriesStart, includeStart, returnSeries.getLatestTime(), true);
  }
}
