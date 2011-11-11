/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static com.opengamma.livedata.normalization.MarketDataRequirementNames.ASK;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.BID;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.LAST;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates a best estimate of the current value of a security.
 */
public class MarketValueCalculator implements NormalizationRule {
  
  private static final double TOLERANCE = 0.00001;
  private static final double MAX_ACCEPTABLE_SPREAD_TO_USE_MIDPOINT = 0.05;

  /**
   * Calculates a best estimate of the current value of a security.
   * <p>
   * This value is normally the midpoint of BID and ASK if both BID and ASK are available.
   * If the spread between BID and ASK is very large (more than 5 percent),
   * LAST is used in preference of the midpoint, if available and if
   * it is within the BID/ASK boundaries. Otherwise, the midpoint
   * is used anyway.
   * <p>
   * If no BID and ASK are available, LAST is used.
   * 
   * @param msg  the message to normalize, not null
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @param fieldHistory  the distributor-specific field history which the rule may choose to update, not null 
   * @return {@code msg} with {@link MarketDataRequirementNames#MARKET_VALUE} added,
   * with the value calculated as described above
   */
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    
    FudgeMsg lkv = fieldHistory.getLastKnownValues();
    
    Double bid = msg.getDouble(BID);
    if (bid == null) {
      bid = lkv.getDouble(BID);
    }
    
    Double ask = msg.getDouble(ASK);
    if (ask == null) {
      ask = lkv.getDouble(ASK);
    }
    
    // If we have seen bid & ask in the past, use bid & ask midpoint.
    if (bid != null && ask != null) {
      
      // Too big of a spread for midpoint to be meaningful?
      if (Math.abs(bid) > TOLERANCE && (Math.abs(ask - bid) / Math.abs(bid) > MAX_ACCEPTABLE_SPREAD_TO_USE_MIDPOINT)) {
        // Try to resort to last, though if this fails use midpoint anyway.
        Double last = lkv.getDouble(LAST);
        if (last == null) {
          last = msg.getDouble(LAST);
        }
        
        if (last != null) {
          // Ok, last was found. But let's make sure that it's within the bid/ask boundaries.
          if (last < bid) {
            msg.add(MarketDataRequirementNames.MARKET_VALUE, bid);
          } else if (last > ask) {
            msg.add(MarketDataRequirementNames.MARKET_VALUE, ask);
          } else {
            msg.add(MarketDataRequirementNames.MARKET_VALUE, last);
          }
          return msg;
        }
      }
      
      double marketValue = (bid + ask) / 2.0;
      msg.add(MarketDataRequirementNames.MARKET_VALUE, marketValue);
      return msg;
    }
    
    // Since we've not seen a bid & ask for this market data in the past,
    // try using LAST.
    Double last = msg.getDouble(LAST);
    if (last == null) {
      return lastKnownMarketValue(msg, fieldHistory);
    }
    
    msg.add(MarketDataRequirementNames.MARKET_VALUE, last);
    return msg;
  }
  
  /**
   * Tries to populate MARKET_VALUE from the history.
   */
  private MutableFudgeMsg lastKnownMarketValue(
      MutableFudgeMsg msg,
      FieldHistoryStore fieldHistory) {
    
    FudgeMsg lkv = fieldHistory.getLastKnownValues();
    
    Double lastKnownMarketValue = lkv.getDouble(MarketDataRequirementNames.MARKET_VALUE);
    if (lastKnownMarketValue == null) {
      return msg;      
    }
    
    msg.add(MarketDataRequirementNames.MARKET_VALUE, lastKnownMarketValue);
    return msg;
  }

}
