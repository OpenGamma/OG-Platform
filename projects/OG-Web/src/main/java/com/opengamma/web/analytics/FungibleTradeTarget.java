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
public class FungibleTradeTarget extends PositionTarget {

  /** ID of the position that contains the trade. */
  //private final UniqueId _parentPositionId;

  /* package */ FungibleTradeTarget(String name, UniqueId id/*, UniqueId parentPositionId*/) {
    super(name, id);
    //_parentPositionId = parentPositionId;
  }

  /**
   * @return ID of the position that contains the trade
   */
  /*public UniqueId getParentPositionId() {
    return _parentPositionId;
  }*/
}
