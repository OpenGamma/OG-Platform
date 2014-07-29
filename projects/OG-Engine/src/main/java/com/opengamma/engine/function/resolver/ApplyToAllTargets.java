/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTarget;

/**
 * Computation target filter that applies to all nodes in the dependency graph.
 */
public final class ApplyToAllTargets implements ComputationTargetFilter {

  /**
   * Standard instance, singleton not enforced.
   */
  public static final ApplyToAllTargets INSTANCE = new ApplyToAllTargets();

  /**
   * Creates an instance. Use the static constant where possible.
   */
  public ApplyToAllTargets() {
  }

  @Override
  public boolean accept(final ComputationTarget target) {
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ApplyToAllTargets;
  }

  @Override
  public int hashCode() {
    return ApplyToAllTargets.class.hashCode();
  }

}
