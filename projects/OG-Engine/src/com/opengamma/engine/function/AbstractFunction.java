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

    @Override
    public final FunctionDefinition getFunctionDefinition() {
      return AbstractFunction.this;
    }

    @Override
    public Set<ValueSpecification> getRequiredLiveData() {
      return getRequiredLiveDataImpl();
    }

    protected void setEarliestInvocationTime(final InstantProvider timestamp) {
      _earliestInvocationTime = (timestamp != null) ? timestamp.toInstant() : null;
    }

    protected void setLatestInvocationTime(final InstantProvider timestamp) {
      _latestInvocationTime = (timestamp != null) ? timestamp.toInstant() : null;
    }

    @Override
    public final InstantProvider getEarliestInvocationTime() {
      return _earliestInvocationTime;
    }

    @Override
    public final InstantProvider getLatestInvocationTime() {
      return _latestInvocationTime;
    }

  }

  /**
   * Extension to {@link AbstractCompiledFunction} that also implements the {@link FunctionInvoker}
   * interface for functions that can be invoked directly at the local node.
   */
  protected abstract class AbstractInvokingCompiledFunction extends AbstractCompiledFunction implements FunctionInvoker {

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
   * @param uniqueIdentifier the uniqueIdentifier to set
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    if (_uniqueIdentifier != null) {
      throw new IllegalStateException("Function unique ID already set");
    }
    _uniqueIdentifier = uniqueIdentifier;
  }

  @Override
  public void init(FunctionCompilationContext context) {
  }

  protected static Set<ValueSpecification> getRequiredLiveDataImpl() {
    return Collections.emptySet();
  }

  @Override
  public FunctionParameters getDefaultParameters() {
    // by default, a function has no parameters.
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
    public final CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
      return this;
    }

    @Override
    public final InstantProvider getEarliestInvocationTime() {
      return null;
    }

    @Override
    public final InstantProvider getLatestInvocationTime() {
      return null;
    }

  }

  /**
   * Extension to {@link AbstractFunction} that does not require time based compilation and may invoke
   * the underlying function.
   */
  public abstract static class NonCompiledInvoker extends NonCompiled implements FunctionInvoker {

    @Override
    public final FunctionInvoker getFunctionInvoker() {
      return this;
    }

  }

}
