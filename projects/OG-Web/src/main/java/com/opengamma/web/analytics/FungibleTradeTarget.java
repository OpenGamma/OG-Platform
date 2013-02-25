/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * {@link RowTarget} implementation for rows displaying data for a trade.
 */
public class FungibleTradeTarget extends RowTarget {

  /** The trade ID */
  private final UniqueId _tradeId;
  /** The position ID */
  private final UniqueId _positionId;

  /* package */ FungibleTradeTarget(String name, UniqueId nodeId, UniqueId positionId, UniqueId tradeId) {
    super(name, nodeId);
    _tradeId = tradeId;
    _positionId = positionId;
  }

  /**
   * @return The trade ID
   */
  public UniqueId getPositionId() {
    return _positionId;
  }

  /**
   * @return The trade ID
   */
  public UniqueId getTradeId() {
    return _tradeId;
  }
}
