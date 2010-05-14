/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static com.opengamma.livedata.normalization.MarketDataFieldNames.ASK_IMPLIED_VOLATILITY_FIELD;
import static com.opengamma.livedata.normalization.MarketDataFieldNames.BEST_IMPLIED_VOLATILITY_FIELD;
import static com.opengamma.livedata.normalization.MarketDataFieldNames.BID_IMPLIED_VOLATILITY_FIELD;
import static com.opengamma.livedata.normalization.MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD;
import static com.opengamma.livedata.normalization.MarketDataFieldNames.LAST_IMPLIED_VOLATILITY_FIELD;
import static com.opengamma.livedata.normalization.MarketDataFieldNames.MID_IMPLIED_VOLATILITY_FIELD;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates a best estimate of the implied volatility of an option. 
 *
 * @author pietari
 */
public class ImpliedVolatilityCalculator implements NormalizationRule {
  
  @Override
  public MutableFudgeFieldContainer apply(
      MutableFudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    
    Double impliedVolatility = msg.getDouble(BEST_IMPLIED_VOLATILITY_FIELD);
    if (impliedVolatility != null) {
      msg.add(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD, impliedVolatility);
      return msg;
    }
    
    impliedVolatility = msg.getDouble(MID_IMPLIED_VOLATILITY_FIELD);
    if (impliedVolatility != null) {
      msg.add(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD, impliedVolatility);
      return msg;
    }
    
    impliedVolatility = msg.getDouble(LAST_IMPLIED_VOLATILITY_FIELD);
    if (impliedVolatility != null) {
      msg.add(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD, impliedVolatility);
      return msg;
    }
    
    Double impliedVolatilityBid = msg.getDouble(BID_IMPLIED_VOLATILITY_FIELD);
    Double impliedVolatilityAsk = msg.getDouble(ASK_IMPLIED_VOLATILITY_FIELD);
    
    if (impliedVolatilityBid != null && impliedVolatilityAsk != null) {
      impliedVolatility = (impliedVolatilityBid + impliedVolatilityAsk) / 2;
      msg.add(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD, impliedVolatility);
      return msg;
    }
    
    FudgeFieldContainer lkv = fieldHistory.getLastKnownValues();
    impliedVolatility = lkv.getDouble(IMPLIED_VOLATILITY_FIELD);
    if (impliedVolatility != null) {
      msg.add(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD, impliedVolatility);
      return msg;
    }
    
    return msg;
  }

}
