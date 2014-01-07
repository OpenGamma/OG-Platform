/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;

/**
 * Function to source time series data for each of the instruments in a curve from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class FXForwardCurveHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurveHistoricalTimeSeriesFunction.class);

  private static String parseString(final String str) {
    if (str.length() == 0) {
      return null;
    }
    return str;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String dataField = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String resolutionKey = parseString(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY));
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final LocalDate endDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY));
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    final FXForwardCurveDefinition definition = (FXForwardCurveDefinition) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION);
    final FXForwardCurveSpecification specification = (FXForwardCurveSpecification) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_SPECIFICATION);
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    final List<Tenor> tenors = definition.getTenors();
    final FXForwardCurveInstrumentProvider curveInstrumentProvider = specification.getCurveInstrumentProvider();
    for (final Tenor tenor : tenors) {
      final ExternalIdBundle id = ExternalIdBundle.of(curveInstrumentProvider.getInstrument(endDate, tenor));
      final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
      if (timeSeries != null) {
        if (timeSeries.getTimeSeries().isEmpty()) {
          s_logger.warn("Time series for {} is empty", id);
        } else {
          bundle.add(dataField, id, timeSeries);
        }
      } else {
        s_logger.warn("Couldn't get time series for {}", id);
      }
    }
    final ExternalIdBundle id = ExternalIdBundle.of(curveInstrumentProvider.getSpotInstrument());
    final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
    if (timeSeries != null) {
      if (timeSeries.getTimeSeries().isEmpty()) {
        s_logger.warn("Time series for {} is empty", id);
      } else {
        bundle.add(dataField, id, timeSeries);
      }
    } else {
      s_logger.warn("Couldn't get time series for {}", id);
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(),
        desiredValue.getConstraints()), bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    Set<String> values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      constraints = desiredValue.getConstraints().copy().with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE);
    } else if (values.size() > 1) {
      constraints = desiredValue.getConstraints().copy().withoutAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY).with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "Null");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "Now");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    if (constraints == null) {
      // We can satisfy the desired value as-is, just ask for the FX forward curve definition and specification to drive our behavior
      final ValueProperties curveConstraints;
      values = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      if (values != null) {
        if (values.isEmpty()) {
          curveConstraints = ValueProperties.withAny(ValuePropertyNames.CURVE).get();
        } else {
          curveConstraints = ValueProperties.with(ValuePropertyNames.CURVE, values).get();
        }
      } else {
        curveConstraints = ValueProperties.none();
      }
      final Set<ValueRequirement> requirements = new HashSet<>();
      requirements.add(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_SPECIFICATION, target.toSpecification(), curveConstraints));
      requirements.add(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, target.toSpecification(), curveConstraints));
      return requirements;
    }
    // We need to substitute ourselves with the adjusted constraints
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    if (ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES.equals(input.getValueName())) {
      // Use the substituted result
      return Collections.singleton(input);
    }
    // Use full results - graph builder will compose correctly against the desired value
    return getResults(context, target);
  }

}
