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
/** Returns the next dividend date of an equity security */
public class DividendFrequencyCodeCalculator implements NormalizationRule {

  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    
    Integer freq = msg.getInt(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
    if (freq == null) {
      FudgeMsg lkv = fieldHistory.getLastKnownValues();
      freq = lkv.getInt(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
    }
    if (freq != null) {
      msg.add(MarketDataRequirementNames.DIVIDEND_FREQUENCY, freq);
      return msg;
    }

    // Fall back to last known market value - may not be needed anymore
    return lastKnownMarketValue(msg, fieldHistory);
  }

  /**
   * Tries to populate DIVIDEND_YIELD from the history.
   */
  private MutableFudgeMsg lastKnownMarketValue(
      MutableFudgeMsg msg,
      FieldHistoryStore fieldHistory) {

    FudgeMsg lkv = fieldHistory.getLastKnownValues();

    Integer lastKnownMarketValue = lkv.getInt(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
    if (lastKnownMarketValue == null) {
      return msg;
    }

    msg.add(MarketDataRequirementNames.DIVIDEND_FREQUENCY, lastKnownMarketValue);
    return msg;
  }
}