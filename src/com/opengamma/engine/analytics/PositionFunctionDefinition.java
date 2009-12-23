/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;

/**
 * 
 *
 * @author kirk
 */
public interface PositionFunctionDefinition
extends FunctionDefinition {

  /**
   * Determine whether this function is applicable to a position of the type provided.
   * While leaf-level positions will customarily be checked using a call
   * to {@link #isApplicableTo(String)} to check on the security type,
   * this method is more appropriate for checking whether a particular function
   * is suitable to {@link PortfolioNode}s. For example, some operations
   * simply may not make sense in an aggregate position.
   * 
   * @param position The position to check.
   * @return {@code true} iff this function is potentially applicable to
   *         the provided position.
   */
  boolean isApplicableTo(Position position);
  
  Collection<AnalyticValueDefinition<?>> getPossibleResults(Position position);
  
  Collection<AnalyticValueDefinition<?>> getInputs(Position position);
  
  DependencyNode buildSubGraph(
      Position position,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver);
  
}
