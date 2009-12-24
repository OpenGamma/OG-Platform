/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.security.Security;

/**
 * The base class from which most {@link FunctionDefinition} implementations
 * should inherit.
 *
 * @author kirk
 */
public abstract class AbstractSecurityFunction extends AbstractFunction implements SecurityFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      Security security,
      FunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
