/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;

/**
 * 
 *
 * @author kirk
 */
public interface PrimitiveAnalyticFunctionDefinition extends
    FunctionDefinition {

  Collection<AnalyticValueDefinition<?>> getPossibleResults();
  
  Collection<AnalyticValueDefinition<?>> getInputs();
  
  DependencyNode buildSubGraph(
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver);
}
