/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeMsg;

/**
 * Default implementation of {@link LastKnownValueStore} that backs onto a
 * {@link FudgeHistoryStore}, which is backed by a {@code Map}. 
 */
public class MapLastKnownValueStore implements LastKnownValueStore {
  private final FieldHistoryStore _historyStore = new FieldHistoryStore();

  @Override
  public void updateFields(FudgeMsg fieldValues) {
    _historyStore.liveDataReceived(fieldValues);
  }

  @Override
  public FudgeMsg getFields() {
    return _historyStore.getLastKnownValues();
  }

  @Override
  public boolean isEmpty() {
    return _historyStore.isEmpty();
  }

}
