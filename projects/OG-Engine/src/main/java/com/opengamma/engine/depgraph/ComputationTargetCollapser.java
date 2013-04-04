/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;

/**
 * Allows two mutually compatible targets to be collapsed into a single one. This can be used to merge two dependency nodes with the function into a single node. If the two original nodes operate on
 * the same target, the test is mainly to check whether the function can produce the union of outputs at lower overall cost than two separate nodes. If the two original nodes operate on different
 * targets, the test is whether a substitute target (all output specifications will be rewritten) would produce the union of the outputs at lower overall cost than two separate nodes.
 */
public interface ComputationTargetCollapser {

  /**
   * Tests whether a collapse operation is supported for the given target. This is intended as a cheap operation to avoid the overhead of identifying candidate nodes for collapse when it is known that
   * no collapse will ever be possible.
   * 
   * @param target the target to test, not null
   * @return false if there are no target specifications that could ever be collapsed into a single node for this target, true if one may be possible
   */
  boolean canApplyTo(ComputationTargetSpecification target);

  /**
   * Collapses two mutually compatible targets to a single target that will be suitable for use as either original target. The two targets to test will be of the same (simplified) target type.
   * {@link #canApplyTo} will have been called on either {@code a} or {@code b} and will have returned true.
   * 
   * @param function the function being applied to the target, not null
   * @param a the first target to test, not null
   * @param b the second target to test, not null
   * @return the collapsed target, or null if it is not possible to collapse these targets
   */
  ComputationTargetSpecification collapse(CompiledFunctionDefinition function, ComputationTargetSpecification a, ComputationTargetSpecification b);

}
