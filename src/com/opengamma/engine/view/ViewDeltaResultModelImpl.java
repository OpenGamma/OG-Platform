/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import com.opengamma.engine.position.Position;

/**
 * 
 *
 * @author kirk
 */
public class ViewDeltaResultModelImpl implements ViewDeltaResultModel,
    Serializable {
  private final Collection<Position> _newPositions = new HashSet<Position>();
  private final Collection<Position> _removedPositions = new HashSet<Position>();
  private final Collection<Position> _allPositions = new HashSet<Position>();
  private long _inputDataTimestamp;
  private long _resultTimestamp;
  private long _previousResultTimestamp;

  /**
   * @return the inputDataTimestamp
   */
  public long getInputDataTimestamp() {
    return _inputDataTimestamp;
  }

  /**
   * @param inputDataTimestamp the inputDataTimestamp to set
   */
  public void setInputDataTimestamp(long inputDataTimestamp) {
    _inputDataTimestamp = inputDataTimestamp;
  }

  /**
   * @return the resultTimestamp
   */
  public long getResultTimestamp() {
    return _resultTimestamp;
  }

  /**
   * @param resultTimestamp the resultTimestamp to set
   */
  public void setResultTimestamp(long resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }

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
  
  public void addNewPosition(Position position) {
    assert position != null;
    _newPositions.add(position);
  }
  
  public void addRemovedPosition(Position position) {
    assert position != null;
    _removedPositions.add(position);
  }
  
  public void addPosition(Position position) {
    assert position != null;
    _allPositions.add(position);
  }
  
}
