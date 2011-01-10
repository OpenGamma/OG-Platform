/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.depgraph.DependencyNode;

/**
 * 
 */
public class ApplyToAllTargets implements ComputationTargetFilter {
  
  /**
   * Since the class has no state, you can always use this instance.
   */
  public static final ApplyToAllTargets INSTANCE = new ApplyToAllTargets();

  @Override
  public boolean accept(DependencyNode node) {
    return true;
  }
  
}
