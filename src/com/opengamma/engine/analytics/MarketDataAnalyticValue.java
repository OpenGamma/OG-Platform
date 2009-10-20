/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

import org.fudgemsg.FudgeMsg;


/**
 * 
 *
 * @author kirk
 */
public class MarketDataAnalyticValue extends
    AnalyticValueImpl<FudgeMsg> {
  public static final String INDICATIVE_VALUE_NAME = "IndicativeValue";

  /**
   * @param definition
   * @param value
   */
  public MarketDataAnalyticValue(
      AnalyticValueDefinition<FudgeMsg> definition,
      FudgeMsg value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<FudgeMsg> scaleForPosition(BigDecimal quantity) {
    // We can't be scaled.
    return this;
  }

}
