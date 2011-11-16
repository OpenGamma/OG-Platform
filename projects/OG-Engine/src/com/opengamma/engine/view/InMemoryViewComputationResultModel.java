/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;

/**
 * 
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {
  
  private static final long serialVersionUID = 1L;
  
  private final Set<ComputedValue> _allMarketData = new HashSet<ComputedValue>();
  
  public void addMarketData(ComputedValue marketData) {
    _allMarketData.add(marketData);    
  }
  
  public Set<ComputedValue> getAllMarketData() {
    return Collections.unmodifiableSet(_allMarketData);
  }

}
