/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A row in the grid. TODO subclass(es) for trades with trade & position ID?
 * also security only belongs in position and trade rows, not nodes. do we really care?
 */
/* package */ class PortfolioGridRow extends MainGridStructure.Row {

  // TODO should these be ObjectIds?
  /** The row's security, null if the row represents a node in the portfolio structure. */
  private final UniqueId _securityId;
  /** The row's underlying security ID, null if there's no underlying or this isn't a trade or position row. */
  private final UniqueId _underlyingId;
  /** The node ID of the row (if it's a noderow ) or its parent node (if it's a position or trade row). */
  private final UniqueId _nodeId;
  /** The position ID of the row (if it's a position row) or its parent position (if it's a trade row). */
  private final UniqueId _positionId;
  /** The row's trade ID (if it's a trade row). */
  private final UniqueId _tradeId;

  /**
   * For rows representing portfolio nodes which have no security or quantity
   * @param target The row's target
   * @param name The row name
   */
  /* package */ PortfolioGridRow(ComputationTargetSpecification target, String name, UniqueId nodeId) {
    super(target, name);
    ArgumentChecker.notNull(nodeId, "nodeId");
    _securityId = null;
    _underlyingId = null;
    _nodeId = nodeId;
    _positionId = null;
    _tradeId = null;
  }

  /**
   * For rows representing position nodes which have a security and quantity
   * @param target The row's target
   * @param securityId The position's security ID, not null
   */
  /* package */ PortfolioGridRow(ComputationTargetSpecification target,
                                 String name,
                                 UniqueId securityId,
                                 //UniqueId underlyingId,
                                 UniqueId nodeId,
                                 UniqueId positionId) {
    super(target, name);
    ArgumentChecker.notNull(securityId, "securityId");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(positionId, "positionId");
    _securityId = securityId;
    _underlyingId = null;
    //_underlyingId = underlyingId;
    _nodeId = nodeId;
    _positionId = positionId;
    _tradeId = null;
  }

  /**
   * For rows representing position nodes which have a security and quantity
   * @param target The row's target
   * @param securityId The position's security ID, not null
   */
  /* package */ PortfolioGridRow(ComputationTargetSpecification target,
                                 String name,
                                 UniqueId securityId,
                                 //UniqueId underlyingId,
                                 UniqueId nodeId,
                                 UniqueId positionId,
                                 UniqueId tradeId) {
    super(target, name);
    ArgumentChecker.notNull(securityId, "securityId");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(positionId, "positionId");
    ArgumentChecker.notNull(tradeId, "tradeId");
    _securityId = securityId;
    _underlyingId = null;
    //_underlyingId = underlyingId;
    _nodeId = nodeId;
    _positionId = positionId;
    _tradeId = tradeId;
  }

  /* package */ UniqueId getSecurityId() {
    return _securityId;
  }

  /* package */ UniqueId getUnderlyingId() {
    return _underlyingId;
  }

  /* package */ UniqueId getNodeId() {
    return _nodeId;
  }

  /* package */ UniqueId getPositionId() {
    return _positionId;
  }

  /* package */ UniqueId getTradeId() {
    return _tradeId;
  }
}
