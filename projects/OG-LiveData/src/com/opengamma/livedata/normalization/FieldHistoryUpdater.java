/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Stores the current state of the message normalization pipeline in the history store.
 * Depending on where in the chain of normalization rules this
 * rule is inserted, the message stored in the history could
 * either be completely unnormalized, partially normalized,
 * or fully normalized.   
 *
 * @author pietari
 */
public class FieldHistoryUpdater implements NormalizationRule {

  @Override
  public MutableFudgeFieldContainer apply(MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    fieldHistory.liveDataReceived(msg);
    return msg;
  }
  
}
