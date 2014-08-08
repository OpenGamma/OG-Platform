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

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.function.Function;
import com.opengamma.util.tuple.Triple;

/**
 * Base class for functions to shift historical market data values, implemented using properties and constraints.
 *
 * @param <T> the type of data converted
 */
public abstract class AbstractHistoricalTimeSeriesShiftFunction<T> extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractHistoricalTimeSeriesShiftFunction.class);

  /**
   * Property to shift a time series.
   */
  protected static final String SHIFT_PROPERTY = "SHIFT";

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // The unique identifier of the time series
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    return super.createValueProperties().withAny(SHIFT_PROPERTY);
  }

  protected abstract ValueSpecification getResult(ComputationTargetSpecification targetSpecification);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(getResult(target.toSpecification()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> shift = constraints.getValues(SHIFT_PROPERTY);
    if ((shift == null) || shift.isEmpty() || constraints.isOptional(SHIFT_PROPERTY)) {
      return null;
    }
    final ValueProperties properties = desiredValue.getConstraints().copy().withoutAny(SHIFT_PROPERTY).with(SHIFT_PROPERTY, "0").withOptional(SHIFT_PROPERTY).get();
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueProperties properties = input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).withAny(SHIFT_PROPERTY).get();
    return Collections.singleton(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), properties));
  }

  private ValueRequirement createRequirement(final FunctionExecutionContext context, final String field, final ExternalIdBundle identifiers) {
    return new ValueRequirement(field, ComputationTargetType.SECURITY, identifiers);
  }

  protected HistoricalTimeSeries applyOverride(final FunctionExecutionContext context, final OverrideOperation operation, final String field, final ExternalIdBundle identifiers,
      final HistoricalTimeSeries value) {
    final ValueRequirement requirement = createRequirement(context, field, identifiers);
    s_logger.debug("Synthetic requirement {} on {}", requirement, value);
    return new SimpleHistoricalTimeSeries(value.getUniqueId(), value.getTimeSeries().operate(new UnaryOperator() {
      @Override
      public double operate(final double a) {
        return (Double) operation.apply(requirement, a);
      }
    }));
  }

  protected Double applyOverride(final FunctionExecutionContext context, final OverrideOperation operation, final String field, final ExternalIdBundle identifiers, final Double value) {
    final ValueRequirement requirement = createRequirement(context, field, identifiers);
    s_logger.debug("Synthetic requirement {} on {}", requirement, value);
    return (Double) operation.apply(requirement, value);
  }

  protected HistoricalTimeSeriesBundle applyOverride(final FunctionExecutionContext context, final OverrideOperation operation, final HistoricalTimeSeriesBundle value) {
    return value.apply(new Function<Triple<String, ExternalIdBundle, HistoricalTimeSeries>, HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries apply(Triple<String, ExternalIdBundle, HistoricalTimeSeries> triple) {
        return applyOverride(context, operation, triple.getFirst(), triple.getSecond(), triple.getThird());
      }
    });
  }

  protected abstract T apply(FunctionExecutionContext context, OverrideOperation operation, T value, ValueSpecification valueSpec);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue input = inputs.getAllValues().iterator().next();
    @SuppressWarnings("unchecked")
    final T inputValue = (T) input.getValue();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String shift = desiredValue.getConstraint(SHIFT_PROPERTY);
    final OverrideOperationCompiler compiler = OpenGammaExecutionContext.getOverrideOperationCompiler(executionContext);
    if (compiler == null) {
      throw new IllegalStateException("No override operation compiler for " + shift + " in execution context");
    }
    s_logger.debug("Applying {} to yield curve {}", shift, inputValue);
    final T result = apply(executionContext, compiler.compile(shift, executionContext.getComputationTargetResolver()), inputValue, input.getSpecification());
    s_logger.debug("Got result {}", result);
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), result));
  }

}
