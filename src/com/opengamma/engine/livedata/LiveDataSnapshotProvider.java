/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Allows a view to snapshot and then query the state of financial data.
 *
 * @author kirk
 */
public interface LiveDataSnapshotProvider {

  void addSubscription(String userName, ValueRequirement valueRequirement);
  void addSubscription(String userName, Set<ValueRequirement> valueRequirements);
  
  long snapshot();

  Object querySnapshot(long snapshot, ValueRequirement requirement); 
  
  void releaseSnapshot(long snapshot);
}
