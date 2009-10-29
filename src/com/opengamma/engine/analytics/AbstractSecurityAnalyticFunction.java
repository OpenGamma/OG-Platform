/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.security.Security;

/**
 * The base class from which most {@link AnalyticFunctionDefinition} implementations
 * should inherit.
 *
 * @author kirk
 */
public abstract class AbstractSecurityAnalyticFunction extends AbstractAnalyticFunction implements SecurityAnalyticFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }
}
