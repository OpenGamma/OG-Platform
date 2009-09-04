/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
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
    // TODO Auto-generated method stub
    return null;
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
  public AnalyticValue getValue(Position position,
      AnalyticValueDefinition valueDefinition) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<AnalyticValueDefinition, AnalyticValue> getValues(Position position) {
    // TODO Auto-generated method stub
    return null;
  }

}
