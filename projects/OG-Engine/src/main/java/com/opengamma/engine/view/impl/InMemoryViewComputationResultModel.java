/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Simple implementation of {@link ViewComputationResultModel}.
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {

  private static final long serialVersionUID = 1L;

  private final Map<ValueSpecification, ComputedValue> _allMarketData = new HashMap<ValueSpecification, ComputedValue>();

  public InMemoryViewComputationResultModel() {
    super();
  }

  public InMemoryViewComputationResultModel(final ViewResultModel copyFrom) {
    super(copyFrom);
  }

  public void update(final ViewComputationResultModel delta) {
    super.update(delta);
    for (ComputedValue marketData : delta.getAllMarketData()) {
      addMarketData(marketData);
    }
  }

  /**
   * Adds a market data value, replacing any previous item with the same value specification.
   * 
   * @param marketData the market data value, not null
   */
  public void addMarketData(ComputedValue marketData) {
    _allMarketData.put(marketData.getSpecification(), marketData);
  }

  @Override
  public Set<ComputedValue> getAllMarketData() {
    return new HashSet<ComputedValue>(_allMarketData.values());
  }

}
