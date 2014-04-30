/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import com.opengamma.analytics.financial.future.MarkToMarketFuturesCalculator;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class MarkToMarketValueRhoFuturesFunction extends MarkToMarketFuturesFunction<Double> {

  /**
   * @param closingPriceField The field name of the historical time series for price, e.g. "PX_LAST", "Close". Set in *FunctionConfiguration
   * @param costOfCarryField The field name of the historical time series for cost of carry e.g. "COST_OF_CARRY". Set in *FunctionConfiguration
   * @param resolutionKey The key defining how the time series resolution is to occur e.g. "DEFAULT_TSS_CONFIG"
   */
  public MarkToMarketValueRhoFuturesFunction(String closingPriceField, String costOfCarryField, String resolutionKey) {
    super(ValueRequirementNames.VALUE_RHO, MarkToMarketFuturesCalculator.RatesDeltaCalculator.getInstance(), closingPriceField, costOfCarryField, resolutionKey);
  }
  
  @Override
  protected Double applyTradeScaling(final Trade trade, Double value) {
    final double quantity = trade.getQuantity().doubleValue();
    return value * quantity;
  }
}
