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

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A store of historical message field values.
 * <p>
 * At the moment, the field history only stores the last value.
 * This class could be extended in future to store the last N 
 * values, values at a certain interval for the last N minutes, etc. 
 */
public class FieldHistoryStore {

  /**
   * The Fudge context in use.
   */
  private final FudgeContext _context = OpenGammaFudgeContext.getInstance();
  /**
   * The last known values.
   */
  private final Map<String, UnmodifiableFudgeField> _lastKnownValues;

  /**
   * Creates an instance.
   */
  public FieldHistoryStore() {
    _lastKnownValues = new HashMap<String, UnmodifiableFudgeField>();
  }

  /**
   * Creates an instance based on a set of fields.
   * 
   * @param history  the history to copy, not null
   */
  public FieldHistoryStore(FudgeMsg history) {
    this();
    liveDataReceived(history);
  }

  /**
   * Creates an instance copying an existing instance.
   * 
   * @param originalToCopy  the original to copy, not null
   */
  public FieldHistoryStore(FieldHistoryStore originalToCopy) {
    this(originalToCopy.getLastKnownValues());   
  }

  //-------------------------------------------------------------------------
  /**
   * Handles the arrival of a data message, storing the fields in history.
   * <p>
   * The history is stored as a {@code Map} by field name, thus if the message
   * contains multiple fields with the same name, only the last will be stored.
   * 
   * @param msg  the received message, not null
   */
  public synchronized void liveDataReceived(FudgeMsg msg) {
    for (FudgeField field : msg) {
      _lastKnownValues.put(field.getName(), UnmodifiableFudgeField.of(field));
    }
  }

  /**
   * Gets the state of the history store as a single message.
   * 
   * @return the history as a message, not null
   */
  public synchronized FudgeMsg getLastKnownValues() {
    MutableFudgeMsg newMessage = _context.newMessage();
    for (Entry<String, UnmodifiableFudgeField> entry : _lastKnownValues.entrySet()) {
      newMessage.add(entry.getValue());
    }
    return newMessage;
  }

  /**
   * Checks if the history store is empty.
   * 
   * @return true if empty
   */
  public synchronized boolean isEmpty() {
    return _lastKnownValues.isEmpty();
  }

  /**
   * Clears the history store.
   */
  public synchronized void clear() {
    _lastKnownValues.clear();
  }

}
