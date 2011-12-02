/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.UnmodifiableFudgeField;

/**
 * A store of historical message field values.
 * <p>
 * At the moment, the field history only stores the last value.
 * This class could be extended in future to store the last N 
 * values, values at a certain interval for the last N minutes, etc. 
 */
public class FieldHistoryStore {
  
  private final FudgeContext _context = FudgeContext.GLOBAL_DEFAULT;
  private final Map<String, UnmodifiableFudgeField> _lastKnownValues;
  
  public FieldHistoryStore() {
    _lastKnownValues = new HashMap<String, UnmodifiableFudgeField>();
  }
  
  public FieldHistoryStore(FudgeMsg history) {
    this();
    liveDataReceived(history);
  }
  
  public FieldHistoryStore(FieldHistoryStore original) {
    this(original.getLastKnownValues());   
  }
  
  public synchronized void liveDataReceived(FudgeMsg msg) {
    for (FudgeField field : msg) {
      _lastKnownValues.put(field.getName(), UnmodifiableFudgeField.of(field)); //NOTE: duplicates are discarded
    }
  }
  
  public synchronized void clear() {
    _lastKnownValues.clear();
  }
  
  public synchronized boolean isEmpty() {
    return _lastKnownValues.isEmpty();
  }
  
  public synchronized FudgeMsg getLastKnownValues() {
    return getMessage(_lastKnownValues);
  }

  private FudgeMsg getMessage(Map<String, UnmodifiableFudgeField> lastKnownValues) {
    MutableFudgeMsg newMessage = _context.newMessage();
    for (Entry<String, UnmodifiableFudgeField> entry : _lastKnownValues.entrySet()) {
      newMessage.add(entry.getValue());
    }
    return newMessage;
  }
}
