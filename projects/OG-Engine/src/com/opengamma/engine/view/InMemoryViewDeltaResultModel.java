/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import javax.time.Instant;

/**
 * 
 */
public class InMemoryViewDeltaResultModel extends InMemoryViewResultModel implements ViewDeltaResultModel {
  private Instant _previousResultTimestamp;

  /**
   * @return the previousResultTimestamp
   */
  public Instant getPreviousResultTimestamp() {
    return _previousResultTimestamp;
  }

  /**
   * @param previousResultTimestamp the previousResultTimestamp to set
   */
  public void setPreviousResultTimestamp(Instant previousResultTimestamp) {
    _previousResultTimestamp = previousResultTimestamp;
  }
}
