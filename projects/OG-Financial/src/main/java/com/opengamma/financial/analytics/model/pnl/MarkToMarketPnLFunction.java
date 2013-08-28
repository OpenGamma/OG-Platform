/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Function that computes the profit or loss since previous close, 
 * as defined by {@link ValueRequirementNames#HISTORICAL_TIME_SERIES_LATEST}. This will get most recent closing price before today.
 * By intention, this will not be today's close even if it's available. Note that this may be stale, if time series aren't updated nightly, as we take latest value.
 * Illiquid securities do not trade each day..
 * As the name MarkToMarket implies, this simple Function applies to Trades on Exchange-Traded Securities.
 */
// For a method which computes a Closing 'Mid' price, see {@link ClosingMidMarkToMarketPnLFunction}. <p>
public class MarkToMarketPnLFunction extends AbstractMarkToMarketPnLFunction {

  private final String _closingPriceField;

  public MarkToMarketPnLFunction(String closingPriceField, String costOfCarryField) {
    super(costOfCarryField);
    ArgumentChecker.notNull(closingPriceField, "closing price data field");
    _closingPriceField = closingPriceField;
  }

  @Override
  protected Set<ValueRequirement> createReferencePriceRequirement(Security security) {
    ValueRequirement htsReq = HistoricalTimeSeriesFunctionUtils.createHTSLatestRequirement(security, _closingPriceField, null);
    return Collections.singleton(htsReq);
  }

  @Override
  protected Double calculateReferencePrice(FunctionInputs inputs, ComputationTarget target) {
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    for (ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST)) {
        String field = input.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
        if (field.equals(_closingPriceField)) {
          Object value = input.getValue();
          if (value == null) {
            throw new NullPointerException("Did not satisfy time series latest requirement," + _closingPriceField +
                                               ", for security, " + security.getExternalIdBundle());
          }
          return (Double) value;
        }
      }
    }
    throw new NullPointerException("Failed to get reference price");
  }
}
