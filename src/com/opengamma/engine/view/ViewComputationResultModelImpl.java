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
import java.util.Map;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.position.Position;

/**
 * A simple in-memory implementation of {@link ViewComputatinResultModel}.
 *
 * @author kirk
 */
public class ViewComputationResultModelImpl implements
    ViewComputationResultModel, Serializable {
  private final Map<Position, PositionResultModel> _perPositionResults = new HashMap<Position, PositionResultModel>();
  private long _inputDataTimestamp;
  private long _resultTimestamp;

  @Override
  public long getInputDataTimestamp() {
    return _inputDataTimestamp;
  }

  /**
   * @param inputDataTimestamp the inputDataTimestamp to set
   */
  public void setInputDataTimestamp(long inputDataTimestamp) {
    _inputDataTimestamp = inputDataTimestamp;
  }

  @Override
  public Collection<Position> getPositions() {
    return Collections.unmodifiableSet(_perPositionResults.keySet());
  }

  @Override
  public long getResultTimestamp() {
    return _resultTimestamp;
  }

  /**
   * @param resultTimestamp the resultTimestamp to set
   */
  public void setResultTimestamp(long resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }

  @Override
  public AnalyticValue<?> getValue(Position position,
      AnalyticValueDefinition<?> valueDefinition) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      return null;
    } else {
      return perPositionModel.get(valueDefinition);
    }
  }

  @Override
  public Map<AnalyticValueDefinition<?>, AnalyticValue<?>> getValues(Position position) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      return Collections.emptyMap();
    } else {
      return perPositionModel.getAllResults();
    }
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
