/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Stores the current state of the message normalization pipeline in the history store.
 */
public class FieldHistoryUpdater implements NormalizationRule {

  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    fieldHistory.liveDataReceived(msg);
    return msg;
  }
  
}
