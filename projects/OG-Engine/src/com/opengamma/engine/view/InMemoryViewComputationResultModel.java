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
  
  private final Set<ComputedValue> _allLiveData = new HashSet<ComputedValue>();
  
  public void addLiveData(ComputedValue liveData) {
    _allLiveData.add(liveData);    
  }
  
  public Set<ComputedValue> getAllLiveData() {
    return Collections.unmodifiableSet(_allLiveData);
  }
  
}
