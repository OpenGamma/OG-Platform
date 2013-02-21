/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A row in the grid. TODO subclass(es) for trades with trade & position ID?
 * also security only belongs in position and trade rows, not nodes. do we really care?
 */
/* package */ class PortfolioGridRow extends MainGridStructure.Row {

  /** The row's security, null if the row represents a node in the portfolio structure. */
  private final Security _security;
  /** The row's quantity, null for row's that don't represent a position or trade. */
  private final BigDecimal _quantity;
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
    _security = null;
    _quantity = null;
    _nodeId = nodeId;
    _positionId = null;
    _tradeId = null;
  }

  /**
   * For rows representing position nodes which have a security and quantity
   * @param target The row's target
   * @param security The position's security, not null
   * @param quantity The position's quantity, not null
   */
  /* package */ PortfolioGridRow(ComputationTargetSpecification target,
                                 String name,
                                 Security security,
                                 BigDecimal quantity,
                                 UniqueId nodeId,
                                 UniqueId positionId) {
    super(target, name);
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(positionId, "positionId");
    _security = security;
    _quantity = quantity;
    _nodeId = nodeId;
    _positionId = positionId;
    _tradeId = null;
  }

  /**
   * For rows representing position nodes which have a security and quantity
   * @param target The row's target
   * @param security The position's security, not null
   * @param quantity The position's quantity, not null
   */
  /* package */ PortfolioGridRow(ComputationTargetSpecification target,
                                 String name,
                                 Security security,
                                 BigDecimal quantity,
                                 UniqueId nodeId,
                                 UniqueId positionId,
                                 UniqueId tradeId) {
    super(target, name);
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(positionId, "positionId");
    ArgumentChecker.notNull(tradeId, "tradeId");
    _security = security;
    _quantity = quantity;
    _nodeId = nodeId;
    _positionId = positionId;
    _tradeId = tradeId;
  }

  /* package */ Security getSecurity() {
    return _security;
  }

  /* package */ BigDecimal getQuantity() {
    return _quantity;
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
