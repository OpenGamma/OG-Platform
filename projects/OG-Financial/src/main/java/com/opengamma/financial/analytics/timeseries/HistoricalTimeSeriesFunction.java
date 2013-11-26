/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Function to source time series data from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class HistoricalTimeSeriesFunction extends AbstractFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesFunction.class);

  protected static HistoricalTimeSeries executeImpl(final FunctionExecutionContext executionContext, final HistoricalTimeSeriesSource timeSeriesSource,
      final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    
    LocalDate valuationDate = executionContext.getValuationTime().atZone(ZoneId.systemDefault()).toLocalDate();
    if (startDate != null && (includeStart && valuationDate.isBefore(startDate) || !(valuationDate.isAfter(startDate)))) {
      return new SimpleHistoricalTimeSeries(targetSpec.getUniqueId(), ImmutableLocalDateDoubleTimeSeries.builder().build());
    }
    
    LocalDate endDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY));
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    HistoricalTimeSeries hts = timeSeriesSource.getHistoricalTimeSeries(targetSpec.getUniqueId(), startDate, includeStart, endDate, includeEnd);
    final String adjusterString = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
    hts = HistoricalTimeSeriesAdjustment.parse(adjusterString).adjust(hts);
    return hts;
  }

  private class Compiled extends AbstractFunction.AbstractInvokingCompiledFunction {

    public Compiled(final Instant firstValidity, final Instant lastValidity) {
      super(firstValidity, lastValidity);
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE; // UID of the time series
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
          .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
          .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
          .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
          .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE).get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      ValueProperties.Builder constraints = null;
      Set<String> values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
      if ((values == null) || values.isEmpty()) {
        constraints = desiredValue.getConstraints().copy().withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY).with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, "");
      } else if (values.size() > 1) {
        constraints = desiredValue.getConstraints().copy().withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
            .with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, values.iterator().next());
      }
      values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
      if ((values == null) || values.isEmpty()) {
        if (constraints == null) {
          constraints = desiredValue.getConstraints().copy();
        }
        constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "");
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
        constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "");
      }
      values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
      if ((values == null) || (values.size() != 1)) {
        if (constraints == null) {
          constraints = desiredValue.getConstraints().copy();
        }
        constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
      }
      if (constraints == null) {
        // We can satisfy the desired value as-is
        return Collections.emptySet();
      } else {
        // We need to substitute ourselves with the adjusted constraints
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
      }
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      if (inputs.isEmpty()) {
        // Use full results - graph builder will compose correctly against the desired value
        return getResults(context, target);
      } else {
        // Use the substituted result
        return inputs.keySet();
      }
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      Object value = executeImpl(executionContext, timeSeriesSource, targetSpec, desiredValue);
      if (value == null) {
        s_logger.error("Couldn't get time series {}", desiredValue);
        value = MissingInput.MISSING_MARKET_DATA;
      }
      return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), targetSpec, desiredValue.getConstraints()), value));
    }

  }

  // FunctionDefinition

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final OffsetDateTime odt = atInstant.atOffset(ZoneOffset.UTC);
    final OffsetDateTime start = odt.withHour(0).withMinute(0).withSecond(0).withNano(0); // Start of the UTC day
    final OffsetDateTime end = start.plusDays(1).minusNanos(1); // End of the UTC day
    return new Compiled(start.toInstant(), end.toInstant());
  }

}
