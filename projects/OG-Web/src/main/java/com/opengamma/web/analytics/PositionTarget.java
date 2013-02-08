/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * {@link RowTarget} implementation for rows displaying data for a position.
 */
public class PositionTarget extends RowTarget {

  /** The position ID */
  private final UniqueId _positionId;

  /* package */ PositionTarget(String name, UniqueId nodeId, UniqueId positionId) {
    super(name, nodeId);
    _positionId = positionId;
  }

  /**
   * @return The position ID
   */
  public UniqueId getPositionId() {
    return _positionId;
  }
}
