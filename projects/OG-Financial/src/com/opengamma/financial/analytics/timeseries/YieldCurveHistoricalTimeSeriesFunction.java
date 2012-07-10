/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Function to source time series data for each of the instruments in a curve from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class YieldCurveHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveHistoricalTimeSeriesFunction.class);

  private static String parseString(final String str) {
    if (str.length() == 0) {
      return null;
    } else {
      return str;
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String dataField = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String resolutionKey = parseString(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY));
    final LocalDate startDate = DateConstraint.getLocalDate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    LocalDate endDate = DateConstraint.getLocalDate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY));
    if (endDate == null) {
      // If end date was not specified, use the context valuation time, rather than the real "up to present day" we'd get from passing null
      endDate = executionContext.getValuationClock().today();
    }
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    final InterpolatedYieldCurveSpecificationWithSecurities yieldCurve = (InterpolatedYieldCurveSpecificationWithSecurities) inputs.getAllValues().iterator().next().getValue();
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (FixedIncomeStripWithSecurity strip : yieldCurve.getStrips()) {
      final ExternalIdBundle id = ExternalIdBundle.of(strip.getSecurityIdentifier());
      final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
      if (timeSeries != null) {
        bundle.add(dataField, id, timeSeries);
      } else {
        s_logger.warn("Couldn't get time series for {}", id);
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, desiredValue.getTargetSpecification(), desiredValue
        .getConstraints()), bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId uid = target.getUniqueId();
    return (uid != null) && uid.getScheme().equals(Currency.OBJECT_SCHEME);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
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
      // We can satisfy the desired value as-is, just ask for the yield curve specification to drive our behavior
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
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(), curveConstraints));
    } else {
      // We need to substitute ourselves with the adjusted constraints
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    if (ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES.equals(input.getValueName())) {
      // Use the substituted result
      return Collections.singleton(input);
    } else {
      // Use full results - graph builder will compose correctly against the desired value
      return getResults(context, target);
    }
  }

}
