/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractPrimitiveAnalyticFunction extends AbstractAnalyticFunction implements PrimitiveAnalyticFunctionDefinition {
  @Override
  public DependencyNode buildSubGraph(
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }
}
