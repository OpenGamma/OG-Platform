/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps another function definition into a form that can work with one or more of its
 * inputs missing.
 */
public class MissingInputsFunction extends AbstractFunction implements CompiledFunctionDefinition, FunctionInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(MissingInputsFunction.class);

  /**
   * Value of the {@link ValuePropertyNames#AGGREGATION} property when one or more of
   * the inputs may be missing.
   */
  public static final String AGGREGATION_STYLE_MISSING = "MissingInputs";

  /**
   * Value of the {@link ValuePropertyNames#AGGREGATION} property when all of the inputs
   * must be available.
   */
  public static final String AGGREGATION_STYLE_FULL = "Full";

  private final FunctionDefinition _underlyingDefinition;
  private final CompiledFunctionDefinition _underlyingCompiled;
  private final FunctionInvoker _underlyingInvoker;

  public MissingInputsFunction(final FunctionDefinition underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlyingDefinition = underlying;
    if (underlying instanceof CompiledFunctionDefinition) {
      _underlyingCompiled = (CompiledFunctionDefinition) underlying;
      if (underlying instanceof FunctionInvoker) {
        _underlyingInvoker = (FunctionInvoker) underlying;
      } else {
        _underlyingInvoker = null;
      }
    } else {
      _underlyingCompiled = null;
      _underlyingInvoker = null;
    }
  }

  protected MissingInputsFunction(final CompiledFunctionDefinition underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlyingDefinition = underlying.getFunctionDefinition();
    _underlyingCompiled = underlying;
    if (underlying instanceof FunctionInvoker) {
      _underlyingInvoker = (FunctionInvoker) underlying;
    } else {
      _underlyingInvoker = null;
    }
  }

  protected MissingInputsFunction(final FunctionInvoker underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlyingDefinition = null;
    _underlyingCompiled = null;
    _underlyingInvoker = underlying;
  }

  protected MissingInputsFunction create(final CompiledFunctionDefinition underlying) {
    return new MissingInputsFunction(underlying);
  }

  protected MissingInputsFunction create(final FunctionInvoker underlying) {
    return new MissingInputsFunction(underlying);
  }

  protected FunctionDefinition getUnderlyingDefinition() {
    return _underlyingDefinition;
  }

  protected CompiledFunctionDefinition getUnderlyingCompiled() {
    return _underlyingCompiled;
  }

  protected FunctionInvoker getUnderlyingInvoker() {
    return _underlyingInvoker;
  }

  protected String getAggregationStyleMissing() {
    return AGGREGATION_STYLE_MISSING;
  }

  protected String getAggregationStyleFull() {
    return AGGREGATION_STYLE_FULL;
  }

  // AbstractFunction

  @Override
  public void setUniqueId(final String identifier) {
    if (getUnderlyingDefinition() instanceof AbstractFunction) {
      ((AbstractFunction) getUnderlyingDefinition()).setUniqueId(identifier);
    }
    super.setUniqueId(identifier);
  }

  // FunctionDefinition

  @Override
  public void init(final FunctionCompilationContext context) {
    getUnderlyingDefinition().init(context);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    final CompiledFunctionDefinition underlying = getUnderlyingDefinition().compile(context, atInstant);
    if (underlying == getUnderlyingCompiled()) {
      s_logger.debug("Compiling underlying on {} gives self", this);
      return this;
    } else {
      s_logger.debug("Creating delegate for compiled underlying on {}", this);
      return create(underlying);
    }
  }

  @Override
  public String getShortName() {
    return getUnderlyingDefinition().getShortName();
  }

  @Override
  public FunctionParameters getDefaultParameters() {
    return getUnderlyingDefinition().getDefaultParameters();
  }

  // CompiledFunctionDefinition

  @Override
  public FunctionDefinition getFunctionDefinition() {
    return this;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return getUnderlyingCompiled().getTargetType();
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return getUnderlyingCompiled().canApplyTo(context, target);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> underlyingResults = getUnderlyingCompiled().getResults(context, target);
    if (underlyingResults == null) {
      s_logger.debug("Underlying returned null for target {}", target);
      return null;
    }
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(underlyingResults.size());
    for (ValueSpecification underlyingResult : underlyingResults) {
      final ValueProperties.Builder properties = underlyingResult.getProperties().copy();
      properties.with(ValuePropertyNames.AGGREGATION, getAggregationStyleFull(), getAggregationStyleMissing());
      results.add(new ValueSpecification(underlyingResult.getValueName(), underlyingResult.getTargetSpecification(), properties.get()));
    }
    s_logger.debug("Returning results {}", results);
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // User must have requested our aggregation style
    final ValueProperties resultConstraints = desiredValue.getConstraints();
    final Set<String> aggregationStyle = resultConstraints.getValues(ValuePropertyNames.AGGREGATION);
    if (aggregationStyle == null) {
      s_logger.debug("No aggregation requirements on {}", desiredValue);
      return null;
    }
    // Requirement has all constraints asked of us (minus the aggregation style)
    final ValueProperties requirementConstraints = resultConstraints.withoutAny(ValuePropertyNames.AGGREGATION);
    final Set<ValueRequirement> requirements = getUnderlyingCompiled().getRequirements(context, target,
        new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), requirementConstraints));
    s_logger.debug("Returning requirements {} for {}", requirements, desiredValue);
    return requirements;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return getUnderlyingCompiled().canHandleMissingRequirements();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> underlyingResults = getUnderlyingCompiled().getResults(context, target, inputs);
    if (underlyingResults == null) {
      s_logger.debug("Underlying returned null inputs {}", inputs);
      return null;
    }
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(underlyingResults.size() * 2);
    for (ValueSpecification underlyingResult : underlyingResults) {
      final ValueProperties properties = underlyingResult.getProperties();
      if ((properties.getProperties() != null) && properties.getProperties().isEmpty()) {
        results.add(underlyingResult);
      } else {
        final ValueProperties.Builder builder = properties.copy();
        builder.with(ValuePropertyNames.AGGREGATION, getAggregationStyleFull());
        results.add(new ValueSpecification(underlyingResult.getValueName(), underlyingResult.getTargetSpecification(), builder.get()));
        builder.withoutAny(ValuePropertyNames.AGGREGATION).with(ValuePropertyNames.AGGREGATION, getAggregationStyleMissing());
        results.add(new ValueSpecification(underlyingResult.getValueName(), underlyingResult.getTargetSpecification(), builder.get()));
      }
    }
    s_logger.debug("Returning results {} for {}", results, inputs);
    return results;
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
    final Set<ValueSpecification> underlyingOutputs = Sets.newHashSetWithExpectedSize(outputs.size());
    for (ValueSpecification output : outputs) {
      final ValueProperties properties = output.getProperties().withoutAny(ValuePropertyNames.AGGREGATION);
      underlyingOutputs.add(new ValueSpecification(output.getValueName(), output.getTargetSpecification(), properties));
    }
    return getUnderlyingCompiled().getAdditionalRequirements(context, target, inputs, underlyingOutputs);
  }

  @Override
  public Instant getEarliestInvocationTime() {
    return getUnderlyingCompiled().getEarliestInvocationTime();
  }

  @Override
  public Instant getLatestInvocationTime() {
    return getUnderlyingCompiled().getLatestInvocationTime();
  }

  @Override
  public FunctionInvoker getFunctionInvoker() {
    final FunctionInvoker underlying = getUnderlyingCompiled().getFunctionInvoker();
    if (underlying == getUnderlyingInvoker()) {
      return this;
    } else {
      return create(underlying);
    }
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ValueRequirement> underlyingDesired = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desiredValue : desiredValues) {
      final ValueProperties requirementConstraints = desiredValue.getConstraints().withoutAny(ValuePropertyNames.AGGREGATION);
      underlyingDesired.add(new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), requirementConstraints));
    }
    final Set<ComputedValue> underlyingResults = getUnderlyingInvoker().execute(executionContext, inputs, target, underlyingDesired);
    if (underlyingResults == null) {
      return Collections.emptySet();
    }
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(underlyingResults.size());
    for (ComputedValue underlyingResult : underlyingResults) {
      final ValueSpecification resultSpec = underlyingResult.getSpecification();
      final ValueProperties.Builder properties = resultSpec.getProperties().copy();
      properties.with(ValuePropertyNames.AGGREGATION, getAggregationStyleMissing());
      results.add(new ComputedValue(new ValueSpecification(resultSpec.getValueName(), resultSpec.getTargetSpecification(), properties.get()), underlyingResult.getValue()));
      if (inputs.getMissingValues().isEmpty()) {
        properties.withoutAny(ValuePropertyNames.AGGREGATION).with(ValuePropertyNames.AGGREGATION, getAggregationStyleFull());
        results.add(new ComputedValue(new ValueSpecification(resultSpec.getValueName(), resultSpec.getTargetSpecification(), properties.get()), underlyingResult.getValue()));
      }
    }
    return results;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

}
