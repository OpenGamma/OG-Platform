/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import it.unimi.dsi.fastutil.Hash.Strategy;

import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.util.PublicAPI;

/**
 * The function metadata associated with a node in a dependency graph.
 */
@PublicAPI
public interface DependencyNodeFunction {

  /**
   * Hashing strategy for using arbitrary implementations in a hash map.
   * <p>
   * Note that this is mainly provided for compatibility between the non-serializable {@link ParameterizedFunction} instances used in resolution rules and graph construction, and
   * {@link DependencyNodeFunctionImpl} instances which may have been returned from a cache or passed another serialization boundary.
   */
  Strategy<DependencyNodeFunction> HASHING_STRATEGY = new Strategy<DependencyNodeFunction>() {

    @Override
    public int hashCode(final DependencyNodeFunction o) {
      return o.getFunctionId().hashCode() * 31 + o.getParameters().hashCode();
    }

    @Override
    public boolean equals(final DependencyNodeFunction a, final DependencyNodeFunction b) {
      if (a == b) {
        return true;
      }
      return a.getFunctionId().equals(b.getFunctionId())
          && a.getParameters().equals(b.getParameters());
    }

  };

  /**
   * Returns the identifier of the function to execute.
   * 
   * @return the function identifier, not null
   */
  String getFunctionId();

  /**
   * Returns the parameters to execute the function with.
   * 
   * @return the function parameters, not null
   */
  FunctionParameters getParameters();

}
