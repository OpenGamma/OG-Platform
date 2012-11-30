/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import com.opengamma.analytics.financial.future.MarkToMarketFuturesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;


/**
 *
 */
public class MarkToMarketForwardFuturesFunction extends MarkToMarketFuturesFunction<Double> {

  public MarkToMarketForwardFuturesFunction() {
    super(ValueRequirementNames.FORWARD, MarkToMarketFuturesCalculator.ForwardPriceCalculator.getInstance());
  }

}
