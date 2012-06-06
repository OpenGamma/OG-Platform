/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;

/**
 * Computation target filter that applies to all nodes in the dependency graph.
 */
public final class ApplyToAllTargets extends ComputationTargetFilter {

  /**
   * Standard instance, singleton not enforced.
   */
  public static final ApplyToAllTargets INSTANCE = new ApplyToAllTargets();

  /**
   * Creates an instance.
   * Use the static constant where possible.
   */
  public ApplyToAllTargets() {
    super(null);
  }

  @Override
  protected boolean accept(ComputationTargetSpecification target) {
    return accept((ComputationTarget) null);
  }

  @Override
  public boolean accept(ComputationTarget target) {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ApplyToAllTargets;
  }

  @Override
  public int hashCode() {
    return ApplyToAllTargets.class.hashCode();
  }

}
