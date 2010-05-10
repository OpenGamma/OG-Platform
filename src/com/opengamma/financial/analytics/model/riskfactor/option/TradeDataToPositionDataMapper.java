/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.pnl.TradeData;

/**
 * 
 */
public class TradeDataToPositionDataMapper {

  public static double getData(final TradeData tradeData, final ComputationTarget target) {
    switch (tradeData) {
      case NUMBER_OF_CONTRACTS:
        return target.getPosition().getQuantity().doubleValue();
      case OPTION_POINT_VALUE:
        throw new NotImplementedException("Don't know how to get option point value");
      default:
        throw new NotImplementedException("Don't know how to get value for " + tradeData);
    }
  }
}
