/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/**
 * A portfolio node implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
public class LazyTargetResolverPortfolioNode extends LazyTargetResolverObject implements PortfolioNode {

  public LazyTargetResolverPortfolioNode(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected PortfolioNode getResolved() {
    return getResolvedTarget().getPortfolioNode();
  }

  @Override
  public UniqueId getParentNodeId() {
    return getResolved().getParentNodeId();
  }

  @Override
  public int size() {
    return getResolved().size();
  }

  @Override
  public List<PortfolioNode> getChildNodes() {
    return getResolved().getChildNodes();
  }

  @Override
  public List<Position> getPositions() {
    return getResolved().getPositions();
  }

  @Override
  public String getName() {
    return getResolved().getName();
  }

}
