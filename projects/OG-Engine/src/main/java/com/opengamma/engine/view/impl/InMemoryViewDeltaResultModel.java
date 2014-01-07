/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * 
 */
public class InMemoryViewDeltaResultModel extends InMemoryViewResultModel implements ViewDeltaResultModel {

  private static final long serialVersionUID = 1L;

  private Instant _previousResultTimestamp;

  public InMemoryViewDeltaResultModel() {
    super();
  }

  public InMemoryViewDeltaResultModel(final ViewDeltaResultModel copyFrom) {
    super(copyFrom);
    setPreviousCalculationTime(copyFrom.getPreviousResultTimestamp());
  }

  public void update(final ViewDeltaResultModel delta) {
    super.update(delta);
    setPreviousCalculationTime(delta.getPreviousResultTimestamp());
  }

  /**
   * @return the previousResultTimestamp
   */
  @Override
  public Instant getPreviousResultTimestamp() {
    return _previousResultTimestamp;
  }

  /**
   * @param previousResultTimestamp the previousResultTimestamp to set
   */
  public void setPreviousCalculationTime(Instant previousResultTimestamp) {
    _previousResultTimestamp = previousResultTimestamp;
  }
}
