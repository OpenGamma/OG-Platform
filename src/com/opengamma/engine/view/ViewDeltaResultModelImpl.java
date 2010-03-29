/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;


/**
 * 
 *
 * @author kirk
 */
public class ViewDeltaResultModelImpl
extends ViewComputationResultModelImpl
implements ViewDeltaResultModel {
  private long _previousResultTimestamp;

  /**
   * @return the previousResultTimestamp
   */
  public long getPreviousResultTimestamp() {
    return _previousResultTimestamp;
  }

  /**
   * @param previousResultTimestamp the previousResultTimestamp to set
   */
  public void setPreviousResultTimestamp(long previousResultTimestamp) {
    _previousResultTimestamp = previousResultTimestamp;
  }
}
