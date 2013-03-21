/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * The base class from which most {@link FunctionDefinition} implementations should inherit.
 * 
 * @author kirk
 */
@PublicSPI
public abstract class AbstractFunction implements FunctionDefinition {

  /**
   * The base class from which most {@link CompiledFunctionDefinition} implementations returned by a {@link AbstractFunction} derived class should inherit.
   */
  protected abstract class AbstractCompiledFunction implements CompiledFunctionDefinition {

    private Instant _earliestInvocationTime;
    private Instant _latestInvocationTime;

    protected AbstractCompiledFunction() {
    }

    /**
     * Creates an instance.
     * 
     * @param earliestInvocation earliest time this metadata and invoker are valid, null to indicate no lower validity bound
     * @param latestInvocation latest time this metadata and invoker are valid, null to indicate no upper validity bound
     */
    protected AbstractCompiledFunction(final Instant earliestInvocation, final Instant latestInvocation) {
      setEarliestInvocationTime(earliestInvocation);
      setLatestInvocationTime(latestInvocation);
    }

    @Override
    public final FunctionDefinition getFunctionDefinition() {
      return AbstractFunction.this;
    }

    /**
     * Default implementation always returns true - the function is applicable. Overload this if there is a cheap test that should suppress the call to {@link #getResults}.
     * 
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target the Target for which capability is to be tests
     * @return always true
     */
    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return true;
    }

    /**
     * Default implementation returns the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}.
     * 
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target The target for which calculation is desired.
     * @param inputs The resolved inputs to the function.
     * @return the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}
     */
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      return getResults(context, target);
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
        final Set<ValueSpecification> outputs) {
      return getAdditionalRequirementsImpl(context, target, inputs, outputs);
    }

    /**
     * Sets the earliest time that this metadata and invoker will be valid for.
     * 
     * @param timestamp the earliest time, null to indicate no lower validity bound
     */
    public void setEarliestInvocationTime(final Instant timestamp) {
      _earliestInvocationTime = timestamp;
    }

    /**
     * Sets the latest time that this metadata and invoker will be valid for.
     * 
     * @param timestamp the latest time, null to indicate no upper validity bound
     */
    public void setLatestInvocationTime(final Instant timestamp) {
      _latestInvocationTime = timestamp;
    }

    @Override
    public final Instant getEarliestInvocationTime() {
      return _earliestInvocationTime;
    }

    @Override
    public final Instant getLatestInvocationTime() {
      return _latestInvocationTime;
    }

    /**
     * Returns false indicating the requirements to the function must be produced within the dependency graph.
     * 
     * @return always false
     */
    @Override
    public boolean canHandleMissingRequirements() {
      return false;
    }

  }

  /**
   * Extension to {@link AbstractCompiledFunction} that also implements the {@link FunctionInvoker} interface for functions that can be invoked directly at the local node.
   */
  protected abstract class AbstractInvokingCompiledFunction extends AbstractCompiledFunction implements FunctionInvoker {

    protected AbstractInvokingCompiledFunction() {
      super();
    }

    /**
     * Creates an instance.
     * 
     * @param earliestInvocation earliest time this metadata and invoker are valid, null to indicate no lower validity bound
     * @param latestInvocation latest time this metadata and invoker are valid, null to indicate no upper validity bound
     */
    protected AbstractInvokingCompiledFunction(final Instant earliestInvocation, final Instant latestInvocation) {
      super(earliestInvocation, latestInvocation);
    }

    /**
     * Creates an instance.
     * 
     * @param earliestInvocation earliest time this metadata and invoker are valid, null to indicate no lower validity bound
     * @param latestInvocation latest time this metadata and invoker are valid, null to indicate no upper validity bound
     */
    protected AbstractInvokingCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation) {
      super(earliestInvocation.toInstant(), latestInvocation.toInstant());
    }

    /**
     * Returns this instance.
     * 
     * @return this instance
     */
    @Override
    public final FunctionInvoker getFunctionInvoker() {
      return this;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return false;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * The unique identifier of the function.
   */
  private String _uniqueId;

  @Override
  public String getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier for the function. Once set, the identifier cannot be changed.
   * 
   * @param uniqueId the unique identifier to set
   */
  public void setUniqueId(final String uniqueId) {
    if (_uniqueId != null) {
      throw new IllegalStateException("Function unique ID already set");
    }
    _uniqueId = uniqueId;
  }

  @Override
  public String getShortName() {
    // Note: don't use simple name as some are inner classes called "Impl" or similarly unhelpful
    final String s = getClass().getName();
    return s.substring(s.lastIndexOf('.') + 1);
  }

  //-------------------------------------------------------------------------
  /**
   * Default implementation performs no initialization action.
   * 
   * @param context the function compilation context
   * @deprecated See [PLAT-2240]. Sub-classes should avoid overriding this function and use {@code compile} instead.
   */
  @Deprecated
  @Override
  public void init(final FunctionCompilationContext context) {
  }

  /**
   * Default implementation of {@link CompiledFunctionDefinition#getAdditionalRequirements (FunctionCompilationContext, ComputationTarget, Set<ValueSpecification>)}.
   * 
   * @param context the function compilation context
   * @param target the computation target
   * @param inputs the resolved inputs
   * @param outputs the resolved outputs
   * @return the empty set indicating no additional data requirements
   */
  protected static Set<ValueRequirement> getAdditionalRequirementsImpl(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    return Collections.emptySet();
  }

  /**
   * Creates a value property builder populated with the function identifier and otherwise empty.
   * 
   * @return the builder
   */
  protected ValueProperties.Builder createValueProperties() {
    return ValueProperties.with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  /**
   * Default implementation indicates no parameters.
   * 
   * @return an {@link EmptyFunctionParameters} instance
   */
  @Override
  public FunctionParameters getDefaultParameters() {
    return EmptyFunctionParameters.INSTANCE;
  }

  /**
   * Extension to {@link AbstractFunction} that does not require time based compilation.
   */
  public abstract static class NonCompiled extends AbstractFunction implements CompiledFunctionDefinition {

    @Override
    public final FunctionDefinition getFunctionDefinition() {
      return this;
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
        final Set<ValueSpecification> outputs) {
      return getAdditionalRequirementsImpl(context, target, inputs, outputs);
    }

    /**
     * Returns this instance - there is no compile time state.
     * 
     * @param context the function compilation context
     * @param atInstant the compilation time
     * @return this instance
     */
    @Override
    public final CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
      return this;
    }

    /**
     * Default implementation always returns true - the function is applicable. Overload this if there is a cheap test that should suppress the call to {@link #getResults}.
     * 
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target the Target for which capability is to be tests
     * @return always true
     */
    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return true;
    }

    /**
     * Default implementation returns the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}.
     * 
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target The target for which calculation is desired.
     * @param inputs The resolved inputs to the function.
     * @return the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}
     */
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      return getResults(context, target);
    }

    /**
     * Returns null indicating always valid.
     * 
     * @return null always
     */
    @Override
    public final Instant getEarliestInvocationTime() {
      return null;
    }

    /**
     * Returns null indicating always valid.
     * 
     * @return null always
     */
    @Override
    public final Instant getLatestInvocationTime() {
      return null;
    }

    /**
     * Returns false indicating the requirements to the function must be produced within the dependency graph.
     * 
     * @return always false
     */
    @Override
    public boolean canHandleMissingRequirements() {
      return false;
    }

  }

  /**
   * Extension to {@link AbstractFunction} that does not require time based compilation and may invoke the underlying function.
   */
  public abstract static class NonCompiledInvoker extends NonCompiled implements FunctionInvoker {

    /**
     * Returns this instance.
     * 
     * @return this instance
     */
    @Override
    public final FunctionInvoker getFunctionInvoker() {
      return this;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return false;
    }
  }

}
