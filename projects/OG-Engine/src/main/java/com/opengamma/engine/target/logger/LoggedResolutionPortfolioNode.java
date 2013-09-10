/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * Wrapper around a {@link PortfolioNode} instance that will log any deep resolution calls.
 */
public class LoggedResolutionPortfolioNode extends AbstractLoggedResolution<PortfolioNode> implements PortfolioNode {

  public LoggedResolutionPortfolioNode(final PortfolioNode underlying, final ResolutionLogger logger) {
    super(underlying, logger);
  }

  // PortfolioNode

  @Override
  public UniqueId getParentNodeId() {
    return getUnderlying().getParentNodeId();
  }

  @Override
  public int size() {
    return getUnderlying().size();
  }

  @Override
  public List<PortfolioNode> getChildNodes() {
    final List<PortfolioNode> childNodes = getUnderlying().getChildNodes();
    final List<PortfolioNode> result = new ArrayList<PortfolioNode>(childNodes.size());
    for (PortfolioNode childNode : childNodes) {
      //log(ComputationTargetType.PORTFOLIO_NODE, childNode); // [PLAT-4491] Nodes are linked by UID not OID
      result.add(new LoggedResolutionPortfolioNode(childNode, getLogger()));
    }
    return result;
  }

  @Override
  public List<Position> getPositions() {
    final List<Position> positions = getUnderlying().getPositions();
    final List<Position> result = new ArrayList<Position>(positions.size());
    for (Position position : positions) {
      log(ComputationTargetType.POSITION, position);
      result.add(new LoggedResolutionPosition(position, getLogger()));
    }
    return result;
  }

  @Override
  public String getName() {
    return getUnderlying().getName();
  }

}
