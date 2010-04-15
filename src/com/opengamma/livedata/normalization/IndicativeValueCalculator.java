/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static com.opengamma.livedata.normalization.MarketDataFieldNames.*;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates a best estimate of the current value of a security.
 *
 * @author pietari
 */
public class IndicativeValueCalculator implements NormalizationRule {
  
  // REVIEW kirk 2010-04-15 -- Shouldn't these be static/constants? Some reason they're not? 
  private final double _tolerance = 0.00001;
  private final double _maxSpreadToUseMidPoint = 0.05;

  @Override
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    
    FudgeFieldContainer lkv = fieldHistory.getLastKnownValues();
    
    Double lastKnownBid = lkv.getDouble(BID_FIELD);
    Double lastKnownAsk = lkv.getDouble(ASK_FIELD);
    
    // If we have seen bid & ask in the past, use bid & ask midpoint.
    if (lastKnownBid != null && lastKnownAsk != null) {
      
      Double bid = msg.getDouble(BID_FIELD);
      Double ask = msg.getDouble(ASK_FIELD);
      
      // Not a bid/ask update at all?
      if (bid == null && ask == null) {
        return msg;
      }
      
      // If only bid or ask was updated, fill in the other one from history.
      if (bid == null) {
        bid = lastKnownBid;
      }
      if (ask == null) {
        ask = lastKnownAsk;
      }
      
      // Too big of a spread for midpoint to be meaningful?
      if (Math.abs(bid) > _tolerance && (Math.abs(ask - bid) / Math.abs(bid) > _maxSpreadToUseMidPoint)) {
        // Try to resort to last, though if this fails use midpoint anyway.
        Double last = lkv.getDouble(LAST_FIELD);
        if (last != null) {
          // Ok, last was found. But let's make sure that it's within the bid/ask boundaries.
          if (last < bid) {
            msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, bid);
          } else if (last > ask) {
            msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, ask);
          } else {
            msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, last);
          }
          return msg;
        }
      }
      
      double indicativeValue = (bid + ask) / 2.0;
      msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, indicativeValue);
      return msg;
    }
    
    // Since we've not seen a bid & ask for this market data in the past,
    // try using LAST.
    Double last = msg.getDouble(LAST_FIELD);
    if (last == null) {
      return msg;
    }
    
    msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, last);
    return msg;
  }
  
  

}
