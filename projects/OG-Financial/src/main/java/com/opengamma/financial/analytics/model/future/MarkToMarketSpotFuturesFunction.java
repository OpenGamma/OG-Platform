/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Set;

import com.opengamma.analytics.financial.future.MarkToMarketFuturesCalculator;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.future.FutureSecurity;

/**
 *
 */
public class MarkToMarketSpotFuturesFunction extends MarkToMarketFuturesFunction<Double> {

  /**
   * @param closingPriceField The field name of the historical time series for price, e.g. "PX_LAST", "Close". Set in *FunctionConfiguration
   * @param costOfCarryField The field name of the historical time series for cost of carry e.g. "COST_OF_CARRY". Set in *FunctionConfiguration
   * @param resolutionKey The key defining how the time series resolution is to occur e.g. "DEFAULT_TSS_CONFIG"
   */
  public MarkToMarketSpotFuturesFunction(final String closingPriceField, final String costOfCarryField, final String resolutionKey) {
    super(ValueRequirementNames.SPOT, MarkToMarketFuturesCalculator.SpotPriceCalculator.getInstance(), closingPriceField, costOfCarryField, resolutionKey);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final FutureSecurity security = (FutureSecurity)  target.getTrade().getSecurity();
    final ValueRequirement spotAssetRequirement = getSpotAssetRequirement(security);
    if (spotAssetRequirement != null) {
      requirements.add(spotAssetRequirement);
    }
    return requirements;
  }

  @Override
  protected SimpleFutureDataBundle getFutureDataBundle(final FutureSecurity security, final FunctionInputs inputs,
    final HistoricalTimeSeriesBundle timeSeriesBundle, final ValueRequirement desiredValue) {
    final Double marketPrice = getMarketPrice(security, inputs);
    final Double spotUnderlyer = getSpot(inputs);
    return new SimpleFutureDataBundle(null, marketPrice, spotUnderlyer, null, null);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return super.createValueProperties(target)
        .withoutAny(ValuePropertyNames.CURRENCY);
  }


}

