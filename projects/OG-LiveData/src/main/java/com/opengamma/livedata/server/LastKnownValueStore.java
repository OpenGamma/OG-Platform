/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeMsg;

/**
 * Stores the last known value for a particular subscription/distribution pair.
 */
public interface LastKnownValueStore {
  
  void updateFields(FudgeMsg fieldValues);
  
  FudgeMsg getFields();
  
  boolean isEmpty();

}
