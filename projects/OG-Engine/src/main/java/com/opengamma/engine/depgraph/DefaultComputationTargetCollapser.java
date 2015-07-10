/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.function.BinaryOperator;

/**
 * Default implementation of {@link ComputationTargetCollapser} that allows delegate instances to be registered for known target types.
 */
public class DefaultComputationTargetCollapser implements ComputationTargetCollapser {

  private static final class ChainedCollapser implements ComputationTargetCollapser {

    private final ComputationTargetCollapser _first;
    private final ComputationTargetCollapser _second;

    public ChainedCollapser(final ComputationTargetCollapser first, final ComputationTargetCollapser second) {
      _first = first;
      _second = second;
    }

    @Override
    public boolean canApplyTo(final ComputationTargetSpecification target) {
      return _first.canApplyTo(target) || _second.canApplyTo(target);
    }

    @Override
    public ComputationTargetSpecification collapse(final CompiledFunctionDefinition function, final ComputationTargetSpecification a, final ComputationTargetSpecification b) {
      ComputationTargetSpecification result = _first.collapse(function, a, b);
      if (result == null) {
        result = _second.collapse(function, a, b);
      }
      return result;
    }

  }

  private final ComputationTargetTypeMap<ComputationTargetCollapser> _collapsers = new ComputationTargetTypeMap<ComputationTargetCollapser>(
      new BinaryOperator<ComputationTargetCollapser>() {
        @Override
        public ComputationTargetCollapser apply(ComputationTargetCollapser a, ComputationTargetCollapser b) {
          return new ChainedCollapser(a, b);
        }
      });

  /**
   * Creates a new instance with no underlying collapsers. Unless one or more collapser instances are added to it by {@link #addCollapser}, {@link #canApplyTo} will always return false.
   */
  public DefaultComputationTargetCollapser() {
  }

  public void addCollapser(final ComputationTargetType type, final ComputationTargetCollapser collapser) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(collapser, "collapser");
    _collapsers.put(type, collapser);
  }

  protected ComputationTargetCollapser getCollapser(final ComputationTargetType type) {
    return _collapsers.get(type);
  }

  // ComputationTargetCollapser

  @Override
  public boolean canApplyTo(final ComputationTargetSpecification target) {
    final ComputationTargetCollapser collapser = getCollapser(target.getType());
    if (collapser != null) {
      return collapser.canApplyTo(target);
    } else {
      return false;
    }
  }

  @Override
  public ComputationTargetSpecification collapse(final CompiledFunctionDefinition function, final ComputationTargetSpecification a, final ComputationTargetSpecification b) {
    // A and B are the same type, and the type must have been matched for canApplyTo to return true
    return getCollapser(a.getType()).collapse(function, a, b);
  }

}
