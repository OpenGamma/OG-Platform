/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionComparator;
import com.opengamma.engine.position.Position;

/**
 * The model of results for a particular {@link Position} within a
 * {@link ViewComputationResultModelImpl}.
 *
 * @author kirk
 */
public class PositionResultModel implements Serializable {
  private final Position _position;
  private final Map<AnalyticValueDefinition<?>, AnalyticValue<?>> _results = new HashMap<AnalyticValueDefinition<?>, AnalyticValue<?>>();
  
  public PositionResultModel(Position position) {
    if(position == null) {
      throw new NullPointerException("Must specify a valid position.");
    }
    _position = position;
  }

  /**
   * @return the position
   */
  public Position getPosition() {
    return _position;
  }
  
  public Map<AnalyticValueDefinition<?>, AnalyticValue<?>> getAllResults() {
    return Collections.<AnalyticValueDefinition<?>, AnalyticValue<?>>unmodifiableMap(_results);
  }
  
  public void add(AnalyticValue<?> value) {
    if(value == null) {
      throw new NullPointerException("Cannot add a null value.");
    }
    if(value.getDefinition() == null) {
      throw new IllegalArgumentException("Value must have a valid definition.");
    }
    _results.put(value.getDefinition(), value);
  }
  
  public AnalyticValue<?> get(AnalyticValueDefinition<?> definition) {
    if(definition == null) {
      return null;
    }
    for(Map.Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> entry : _results.entrySet()) {
      if(AnalyticValueDefinitionComparator.matches(definition, entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

}
