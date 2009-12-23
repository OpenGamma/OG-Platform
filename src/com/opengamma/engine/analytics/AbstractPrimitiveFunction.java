/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractPrimitiveFunction extends AbstractFunction implements PrimitiveFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

}
