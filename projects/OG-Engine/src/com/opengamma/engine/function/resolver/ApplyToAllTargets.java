/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

}
