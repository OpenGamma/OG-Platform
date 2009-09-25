/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
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
  private final Map<Position, PositionResultModel> _perPositionResults = new HashMap<Position, PositionResultModel>();
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
  
  @Override
  public Collection<Position> getAllPositions() {
    return Collections.unmodifiableCollection(_allPositions);
  }

  @Override
  public Collection<Position> getPositionsWithDeltas() {
    return Collections.unmodifiableCollection(_perPositionResults.keySet());
  }

  @Override
  public Map<AnalyticValueDefinition<?>, AnalyticValue<?>> getDeltaValues(
      Position position) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      return Collections.emptyMap();
    } else {
      return perPositionModel.getAllResults();
    }
  }

  @Override
  public Collection<Position> getNewPositions() {
    return Collections.unmodifiableCollection(_newPositions);
  }

  @Override
  public Collection<Position> getRemovedPositions() {
    return Collections.unmodifiableCollection(_removedPositions);
  }

  public void addValue(Position position, AnalyticValue<?> value) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      perPositionModel = new PositionResultModel(position);
      _perPositionResults.put(position, perPositionModel);
    }
    perPositionModel.add(value);
  }
}
