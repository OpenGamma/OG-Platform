/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

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
public abstract class AbstractPositionFunction extends AbstractFunction implements PositionFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      Position position,
      FunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
