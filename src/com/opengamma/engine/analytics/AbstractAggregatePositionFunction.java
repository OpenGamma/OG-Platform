/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;

/**
 * The base class from which most {@link FunctionDefinition} implementations
 * should inherit.
 *
 * @author kirk
 */
public abstract class AbstractAggregatePositionFunction extends AbstractFunction implements AggregatePositionFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      Collection<Position> positions,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.MULTIPLE_POSITIONS;
  }

}
