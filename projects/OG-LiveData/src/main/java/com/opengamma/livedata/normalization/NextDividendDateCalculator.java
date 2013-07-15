/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Returns the next dividend date of an equity security.
 */
public class NextDividendDateCalculator implements NormalizationRule {

  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    Object dateObject = msg.getValue(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    if (dateObject == null) {
      // fall back to last known value. This is expected as this value does not tick. 
      FudgeMsg lkv = fieldHistory.getLastKnownValues();
      dateObject = lkv.getValue(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
      if (dateObject != null) {
        msg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, dateObject);
      }
    }
    return msg;
  }
}
