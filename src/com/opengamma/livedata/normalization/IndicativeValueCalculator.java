/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static com.opengamma.livedata.normalization.MarketDataFieldNames.*;
import org.fudgemsg.MutableFudgeFieldContainer;

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
  public MutableFudgeFieldContainer apply(MutableFudgeFieldContainer msg) {
    Double bid = msg.getDouble(BID_FIELD);
    Double ask = msg.getDouble(ASK_FIELD);
    
    if (bid != null && ask != null) {
      double indicativeValue = (bid + ask) / 2.0;
      msg.add(MarketDataFieldNames.INDICATIVE_VALUE_FIELD, indicativeValue);
    }
    
    return msg;
  }
  
  

}
