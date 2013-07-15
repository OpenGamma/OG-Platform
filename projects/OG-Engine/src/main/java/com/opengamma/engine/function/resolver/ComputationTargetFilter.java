/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;

/**
 * 
 */
public abstract class ComputationTargetFilter implements DependencyNodeFilter {

  private final ComputationTargetResolver.AtVersionCorrection _computationTargetResolver;

  public ComputationTargetFilter(final ComputationTargetResolver.AtVersionCorrection computationTargetResolver) {
    _computationTargetResolver = computationTargetResolver;
  }

  protected ComputationTargetResolver.AtVersionCorrection getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  @Override
  public boolean accept(final DependencyNode node) {
    return accept(node.getComputationTarget());
  }

  protected boolean accept(final ComputationTargetSpecification target) {
    return accept(getComputationTargetResolver().resolve(target));
  }

  public abstract boolean accept(ComputationTarget target);

}
