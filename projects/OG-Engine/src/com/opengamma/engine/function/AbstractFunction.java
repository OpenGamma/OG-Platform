/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * The base class from which most {@link FunctionDefinition} implementations
 * should inherit.
 *
 * @author kirk
 */
@PublicSPI
public abstract class AbstractFunction implements FunctionDefinition {

  /**
   * The base class from which most {@link CompiledFunctionDefinition} implementations
   * returned by a {@link AbstractFunction} derived class should inherit.
   */
  protected abstract class AbstractCompiledFunction implements CompiledFunctionDefinition {

    private Instant _earliestInvocationTime;
    private Instant _latestInvocationTime;

    protected AbstractCompiledFunction() {
    }

    protected AbstractCompiledFunction(final InstantProvider earliestInvocation, final InstantProvider latestInvocation) {
      setEarliestInvocationTime(earliestInvocation);
      setLatestInvocationTime(latestInvocation);
    }

    @Override
    public final FunctionDefinition getFunctionDefinition() {
      return AbstractFunction.this;
    }

    @Override
    public Set<ValueSpecification> getRequiredLiveData() {
      return getRequiredLiveDataImpl();
    }

    /**
     * Default implementation returns the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}.
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target The target for which calculation is desired.
     * @param inputs The resolved inputs to the function.
     * @return the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}
     */
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs) {
      return getResults(context, target);
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
        final Set<ValueSpecification> outputs) {
      return getAdditionalRequirementsImpl(context, target, inputs, outputs);
    }

    public void setEarliestInvocationTime(final InstantProvider timestamp) {
      _earliestInvocationTime = (timestamp != null) ? timestamp.toInstant() : null;
    }

    public void setLatestInvocationTime(final InstantProvider timestamp) {
      _latestInvocationTime = (timestamp != null) ? timestamp.toInstant() : null;
    }

    @Override
    public final Instant getEarliestInvocationTime() {
      return _earliestInvocationTime;
    }

    @Override
    public final Instant getLatestInvocationTime() {
      return _latestInvocationTime;
    }

  }

  /**
   * Extension to {@link AbstractCompiledFunction} that also implements the {@link FunctionInvoker}
   * interface for functions that can be invoked directly at the local node.
   */
  protected abstract class AbstractInvokingCompiledFunction extends AbstractCompiledFunction implements FunctionInvoker {

    protected AbstractInvokingCompiledFunction() {
      super();
    }

    protected AbstractInvokingCompiledFunction(final InstantProvider earliestInvocation, final InstantProvider latestInvocation) {
      super(earliestInvocation, latestInvocation);
    }

    /**
     * Returns this instance.
     * @return this instance
     */
    @Override
    public final FunctionInvoker getFunctionInvoker() {
      return this;
    }

  }

  private String _uniqueIdentifier;

  /**
   * @return the uniqueIdentifier
   */
  public String getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * @return the short name
   */
  public String getShortName() {
    return getClass().getSimpleName();
  }

  /**
   * @param uniqueIdentifier the uniqueIdentifier to set
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    if (_uniqueIdentifier != null) {
      throw new IllegalStateException("Function unique ID already set");
    }
    _uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Default implementation performs no initialization action.
   * @param context the function compilation context
   */
  @Override
  public void init(FunctionCompilationContext context) {
  }

  protected static Set<ValueSpecification> getRequiredLiveDataImpl() {
    return Collections.emptySet();
  }

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
    return ValueProperties.with(ValuePropertyNames.FUNCTION, getUniqueIdentifier());
  }

  /**
   * Default implementation indicates no parameters.
   * @return an {@link EmptyFunctionParameters} instance
   */
  @Override
  public FunctionParameters getDefaultParameters() {
    return new EmptyFunctionParameters();
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
    public Set<ValueSpecification> getRequiredLiveData() {
      return getRequiredLiveDataImpl();
    }

    @Override
    public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
        final Set<ValueSpecification> outputs) {
      return getAdditionalRequirementsImpl(context, target, inputs, outputs);
    }

    /**
     * Returns this instance - there is no compile time state.
     * @param context the function compilation context
     * @param atInstant the compilation time
     * @return this instance
     */
    @Override
    public final CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
      return this;
    }

    /**
     * Default implementation returns the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}.
     * @param context The compilation context with view-specific parameters and configurations.
     * @param target The target for which calculation is desired.
     * @param inputs The resolved inputs to the function.
     * @return the same results as {@link #getResults (FunctionCompilationContext, ComputationTarget)}
     */
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs) {
      return getResults(context, target);
    }

    /**
     * Returns {@code null} indicating always valid.
     * @return {@code null}
     */
    @Override
    public final Instant getEarliestInvocationTime() {
      return null;
    }

    /**
     * Returns {@code null} indicating always valid.
     * @return {@code null}
     */
    @Override
    public final Instant getLatestInvocationTime() {
      return null;
    }

  }

  /**
   * Extension to {@link AbstractFunction} that does not require time based compilation and may invoke
   * the underlying function.
   */
  public abstract static class NonCompiledInvoker extends NonCompiled implements FunctionInvoker {

    /**
     * Returns this instance.
     * @return this instance
     */
    @Override
    public final FunctionInvoker getFunctionInvoker() {
      return this;
    }

  }

}
