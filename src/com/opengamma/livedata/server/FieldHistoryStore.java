/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * At the moment, the field history only stores the last value.
 * <p>
 * This class could be extended in future to store the last N 
 * values, values at a certain interval for the last N minutes, etc. 
 *
 * @author pietari
 */
public class FieldHistoryStore {
  
  private final FudgeContext CONTEXT = FudgeContext.GLOBAL_DEFAULT;
  private final MutableFudgeFieldContainer _lastKnownValues;
  
  public FieldHistoryStore() {
    _lastKnownValues = CONTEXT.newMessage();
  }
  
  public synchronized void liveDataReceived(FudgeFieldContainer msg) {
    for (FudgeField field : msg.getAllFields()) {
      _lastKnownValues.remove(field.getName());
      _lastKnownValues.add(field);
    }
  }
  
  public synchronized FudgeFieldContainer getLastKnownValues() {
    return _lastKnownValues;
  }

}
