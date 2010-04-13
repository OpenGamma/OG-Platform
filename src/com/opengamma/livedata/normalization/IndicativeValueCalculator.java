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
 * At the moment, the midpoint is always used. This needs to be improved.
 * Ideally the midpoint will be used when the market is active, but
 * otherwise we may resort to the bid, ask, last, or a previously-computed
 * midpoint.
 *
 * @author pietari
 */
public class IndicativeValueCalculator implements NormalizationRule {

  @Override
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    
    FudgeFieldContainer lkv = fieldHistory.getLastKnownValues();
    
    Double bid = msg.getDouble(BID_FIELD);
    if (bid == null) {
      bid = lkv.getDouble(BID_FIELD);
    }
    
    Double ask = msg.getDouble(ASK_FIELD);
    if (ask == null) {
      ask = lkv.getDouble(ASK_FIELD);
    }
    
    if (bid != null && ask != null) {
      double indicativeValue = (bid + ask) / 2.0;
      msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, indicativeValue);
      return msg;
    }
    
    // If no bid/ask found, try last
    Double last = msg.getDouble(LAST_FIELD);
    if (last != null) {
      msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, last);
      return msg;
    }
    
    // Nothing found.
    return msg;
  }
  
  

}
