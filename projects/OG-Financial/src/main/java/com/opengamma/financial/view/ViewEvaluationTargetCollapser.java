/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.ComputationTargetCollapser;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link ComputationTargetCollapser} implementation for {@link ViewEvaluationTarget} instances.
 */
public class ViewEvaluationTargetCollapser implements ComputationTargetCollapser {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewEvaluationTargetCollapser.class);

  private final TempTargetRepository _tempTargets;

  public ViewEvaluationTargetCollapser(final TempTargetRepository tempTargets) {
    ArgumentChecker.notNull(tempTargets, "tempTargets");
    _tempTargets = tempTargets;
  }

  protected TempTargetRepository getTempTargets() {
    return _tempTargets;
  }

  protected ViewEvaluationTarget getTarget(final ComputationTargetSpecification targetSpec) {
    final UniqueIdentifiable targetObject = getTempTargets().get(targetSpec.getUniqueId());
    if (!(targetObject instanceof ViewEvaluationTarget)) {
      s_logger.warn("Invalid ViewEvaluationTarget {} for {}", targetObject, targetSpec);
      return null;
    }
    return (ViewEvaluationTarget) targetObject;
  }

  // ComputationTargetCollapser

  @Override
  public boolean canApplyTo(final ComputationTargetSpecification target) {
    return ViewEvaluationTarget.TYPE.equals(target.getType());
  }

  @Override
  public ComputationTargetSpecification collapse(final CompiledFunctionDefinition function, final ComputationTargetSpecification a, final ComputationTargetSpecification b) {
    s_logger.debug("Request to collapse {} against {}", a, b);
    final ViewEvaluationTarget targetA = getTarget(a);
    if (targetA == null) {
      s_logger.warn("Target {} not found", a);
      return null;
    }
    final ViewEvaluationTarget targetB = getTarget(b);
    if (targetB == null) {
      s_logger.warn("Target {} not found", b);
      return null;
    }
    final ViewEvaluationTarget merged = targetA.union(targetB);
    if (merged == null) {
      s_logger.debug("Can't merge {} and {}", targetA, targetB);
      return null;
    }
    if (merged == targetA) {
      s_logger.debug("A) is a superset - {}", targetA);
      return a;
    }
    if (merged == targetB) {
      s_logger.debug("B) is a superset - {}", targetB);
      return b;
    }
    final UniqueId uid = getTempTargets().locateOrStore(merged);
    s_logger.debug("Created merged target {}", uid);
    return a.replaceIdentifier(uid);
  }

}
