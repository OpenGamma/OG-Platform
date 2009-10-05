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
public abstract class AbstractAnalyticFunction implements AnalyticFunctionDefinition {
  private String _uniqueIdentifier;

  /**
   * @return the uniqueIdentifier
   */
  public String getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * @param uniqueIdentifier the uniqueIdentifier to set
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }
  
  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    // TODO Auto-generated method stub
    return false;
  }

}
