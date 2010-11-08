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
 * A store of historical message field values.
 * <p>
 * At the moment, the field history only stores the last value.
 * This class could be extended in future to store the last N 
 * values, values at a certain interval for the last N minutes, etc. 
 */
public class FieldHistoryStore {
  
  private final FudgeContext _context = FudgeContext.GLOBAL_DEFAULT;
  private final MutableFudgeFieldContainer _lastKnownValues;
  
  public FieldHistoryStore() {
    _lastKnownValues = _context.newMessage();
  }
  
  public FieldHistoryStore(FudgeFieldContainer history) {
    _lastKnownValues = _context.newMessage(history);   
  }
  
  public FieldHistoryStore(FieldHistoryStore original) {
    _lastKnownValues = _context.newMessage(original._lastKnownValues);   
  }
  
  public synchronized void liveDataReceived(FudgeFieldContainer msg) {
    for (FudgeField field : msg.getAllFields()) {
      _lastKnownValues.remove(field.getName());
      _lastKnownValues.add(field);
    }
  }
  
  public void clear() {
    _lastKnownValues.clear();
  }
  
  public boolean isEmpty() {
    return _lastKnownValues.isEmpty();
  }
  
  public synchronized FudgeFieldContainer getLastKnownValues() {
    return _lastKnownValues;
  }

}
