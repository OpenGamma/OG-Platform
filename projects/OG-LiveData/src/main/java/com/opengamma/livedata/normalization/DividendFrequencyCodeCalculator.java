/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
/** 
 * Returns the number of dividends expected to be paid per year.<p>
 * <p>
 * In ActivFinancial, this is provided as an an Enumerated integer code. It is this that we are decoding.
 * So far, we have only come across quarterly payments, which have a code of 2. 
 * As other frequencies are encountered, this will be expanded upon.
 */
public class DividendFrequencyCodeCalculator implements NormalizationRule {

  private static final Logger s_logger = LoggerFactory.getLogger(DividendFrequencyCodeCalculator.class);
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    
    Double freq = msg.getDouble(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
    if (freq != null) {
      if (freq != 2.0) {
        s_logger.warn("Unrecognized dividend frequency {} for security {}. Trivial to add handling for this. Defaulting to 4 / year.", freq, securityUniqueId);
      }
      msg.add(MarketDataRequirementNames.DIVIDEND_FREQUENCY, 4.0);
      return msg;
    } else {
      FudgeMsg lkv = fieldHistory.getLastKnownValues();
      freq = lkv.getDouble(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
      if (freq != null) {
        msg.add(MarketDataRequirementNames.DIVIDEND_FREQUENCY, freq);
        return msg;
      }
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
