/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * {@link RowTarget} for a row that contains an OTC trade / position. There is one position for each OTC trade
 * so they are displayed as a single row.
 */
public class OtcTradeTarget extends RowTarget {

  /** The trade ID */
  private final UniqueId _tradeId;
  /** The position ID */
  private final UniqueId _positionId;

  /**
   * @param name The row name
   * @param nodeId The ID of the portfolio node containing the trade
   * @param positionId The position ID
   * @param tradeId the trade ID
   */
  /* package */ OtcTradeTarget(String name, UniqueId nodeId, UniqueId positionId, UniqueId tradeId) {
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
