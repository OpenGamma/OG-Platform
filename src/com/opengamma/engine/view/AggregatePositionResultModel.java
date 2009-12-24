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

import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionComparator;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;

/**
 * The model of results for a particular {@link Position} within a
 * {@link ViewComputationResultModelImpl}.
 *
 * @author kirk
 */
public class AggregatePositionResultModel implements Serializable {
  private final PortfolioNode _portfolioNode;
  private final Map<AnalyticValueDefinition<?>, ComputedValue<?>> _results = new HashMap<AnalyticValueDefinition<?>, ComputedValue<?>>();
  
  public AggregatePositionResultModel(PortfolioNode portfolioNode) {
    if(portfolioNode == null) {
      throw new NullPointerException("Must specify a valid portfolio node.");
    }
    _portfolioNode = portfolioNode;
  }

  /**
   * @return the portfolio node
   */
  public PortfolioNode getPortfolioNode() {
    return _portfolioNode;
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

}
