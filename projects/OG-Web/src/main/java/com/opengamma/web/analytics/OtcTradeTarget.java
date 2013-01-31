/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 *
 */
public class OtcTradeTarget extends RowTarget {

  /** ID of the position that contains the trade. */
  private final UniqueId _positiond;

  /* package */ OtcTradeTarget(String name, UniqueId id, UniqueId positionId) {
    super(name, id);
    _positiond = positionId;
  }

  /**
   * @return ID of the position that contains the trade
   */
  public UniqueId getPositionId() {
    return _positiond;
  }
}
