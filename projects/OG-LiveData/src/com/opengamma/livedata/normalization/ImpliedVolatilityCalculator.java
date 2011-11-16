/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static com.opengamma.livedata.normalization.MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.BEST_IMPLIED_VOLATILITY;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.BID_IMPLIED_VOLATILITY;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.IMPLIED_VOLATILITY;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY;
import static com.opengamma.livedata.normalization.MarketDataRequirementNames.MID_IMPLIED_VOLATILITY;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates a best estimate of the implied volatility of an option. 
 *
 * @author pietari
 */
public class ImpliedVolatilityCalculator implements NormalizationRule {
  
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    
    Double impliedVolatility = msg.getDouble(BEST_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }
    
    impliedVolatility = msg.getDouble(MID_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }
    
    impliedVolatility = msg.getDouble(LAST_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }
    
    Double impliedVolatilityBid = msg.getDouble(BID_IMPLIED_VOLATILITY);
    Double impliedVolatilityAsk = msg.getDouble(ASK_IMPLIED_VOLATILITY);
    
    if (impliedVolatilityBid != null && impliedVolatilityAsk != null) {
      impliedVolatility = (impliedVolatilityBid + impliedVolatilityAsk) / 2;
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }
    
    FudgeMsg lkv = fieldHistory.getLastKnownValues();
    impliedVolatility = lkv.getDouble(IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }
    
    return msg;
  }

}
