/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;

/**
 * Allows two mutually compatible targets to be collapsed into a single one. This can be used to merge two dependency nodes with the same inputs and the same function into a single node. If the two
 * original nodes operate on the same target, the test is mainly to check whether the function can produce the union of outputs at lower overall cost than two separate nodes. If the two original nodes
 * operate on different targets, the test is whether a substitute target (all output specifications will be rewritten) would produce the union of the outputs at lower overall cost than two separate
 * nodes.
 */
public interface ComputationTargetCollapser {

  /**
   * Collapses two mutually compatible targets to a single target that will be suitable for use as either original target. The two targets to test will be of the same (simplified) target type.
   *
   * @param function the function being applied to the target, not null
   * @param a the first target to test, not null
   * @param b the second target to test, not null
   * @return the collapsed target, or null if it is not possible to collapse these targets
   */
  ComputationTargetSpecification collapse(CompiledFunctionDefinition function, ComputationTargetSpecification a, ComputationTargetSpecification b);

}
