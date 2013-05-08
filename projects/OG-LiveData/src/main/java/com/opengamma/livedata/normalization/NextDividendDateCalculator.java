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
/** Returns the next dividend date of an equity security */
public class NextDividendDateCalculator implements NormalizationRule {
  
  private static final Logger s_logger = LoggerFactory.getLogger(NextDividendDateCalculator.class);
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {

    FudgeMsg lkv = fieldHistory.getLastKnownValues();
    Object dateObject = msg.getValue(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    String dateString = msg.getString(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    // TODO Once dateString isn't null, the idea would be to parse the string into a date. 
    // We can add a LocalDate (or similar) to a FudgeMsg, right?!?
    // s_logger.debug(MarketDataRequirementNames.NEXT_DIVIDEND_DATE + " = " + dateString);
    if (dateString == null) {
      dateString = lkv.getString(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    }
    if (dateString != null) {
      msg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, dateString);
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

    String lastKnownMarketValue = lkv.getString(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    if (lastKnownMarketValue == null) {
      return msg;
    }

    msg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, lastKnownMarketValue);
    return msg;
  }
}
