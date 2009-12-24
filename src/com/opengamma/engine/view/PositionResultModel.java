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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.AnalyticValueDefinitionComparator;
import com.opengamma.engine.value.ComputedValue;

/**
 * The model of results for a particular {@link Position} within a
 * {@link ViewComputationResultModelImpl}.
 *
 * @author kirk
 */
public class PositionResultModel implements Serializable {
  private final Position _position;
  private final Map<AnalyticValueDefinition<?>, ComputedValue<?>> _results = new HashMap<AnalyticValueDefinition<?>, ComputedValue<?>>();
  
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
  
  public Map<AnalyticValueDefinition<?>, ComputedValue<?>> getAllResults() {
    return Collections.<AnalyticValueDefinition<?>, ComputedValue<?>>unmodifiableMap(_results);
  }
  
  public void add(ComputedValue<?> value) {
    if(value == null) {
      throw new NullPointerException("Cannot add a null value.");
    }
    if(value.getDefinition() == null) {
      throw new IllegalArgumentException("Value must have a valid definition.");
    }
    _results.put(value.getDefinition(), value);
  }
  
  public ComputedValue<?> get(AnalyticValueDefinition<?> definition) {
    if(definition == null) {
      return null;
    }
    for(Map.Entry<AnalyticValueDefinition<?>, ComputedValue<?>> entry : _results.entrySet()) {
      if(AnalyticValueDefinitionComparator.matches(definition, entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  public String debugToString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
