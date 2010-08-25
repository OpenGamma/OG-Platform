/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.depgraph.DependencyNode;

/**
 * 
 */
public class ApplyToAllTargets implements ComputationTargetFilter {

  @Override
  public boolean accept(DependencyNode node) {
    return true;
  }
  
}
