/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.WeakInstanceCache;

/**
 * Trivial implementation of {@link DependencyNodeFunction}.
 */
public final class DependencyNodeFunctionImpl implements DependencyNodeFunction, Serializable {

  private static final long serialVersionUID = 1L;

  private static final WeakInstanceCache<DependencyNodeFunctionImpl> s_instances = new WeakInstanceCache<DependencyNodeFunctionImpl>();

  private final String _functionId;

  private final FunctionParameters _parameters;

  private DependencyNodeFunctionImpl(final String functionId, final FunctionParameters parameters) {
    _functionId = functionId;
    _parameters = parameters;
  }

  public static DependencyNodeFunction of(final String functionId, final FunctionParameters parameters) {
    ArgumentChecker.notNull(functionId, "functionId");
    ArgumentChecker.notNull(parameters, "parameters");
    return s_instances.get(new DependencyNodeFunctionImpl(functionId, parameters));
  }

  public static DependencyNodeFunction of(final FunctionDefinition function) {
    return of(function.getUniqueId(), function.getDefaultParameters());
  }

  // DependencyNodeFunction

  @Override
  public String getFunctionId() {
    return _functionId;
  }

  @Override
  public FunctionParameters getParameters() {
    return _parameters;
  }

  // Object

  @Override
  public int hashCode() {
    return _functionId.hashCode() * 31 + _parameters.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DependencyNodeFunctionImpl)) {
      return false;
    }
    final DependencyNodeFunctionImpl other = (DependencyNodeFunctionImpl) o;
    return _functionId.equals(other._functionId) && _parameters.equals(other._parameters);
  }

  @Override
  public String toString() {
    return "Function[" + _functionId + ", " + _parameters + "]";
  }

  // Serializable

  private Object readResolve() throws ObjectStreamException {
    return s_instances.get(this);
  }

}
