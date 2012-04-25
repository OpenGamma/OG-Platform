/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class BloombergForexSpotRateMarketDataFunction extends AbstractForexSpotRateMarketDataFunction {

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(desiredValue.getTargetSpecification().getUniqueId());
    final ValueRequirement spotRequirement;
    // Implementation note: the currency pair order in FX is given by FXUtils.
    if (FXUtils.isInBaseQuoteOrder(currencyPair.getFirstCurrency(), currencyPair.getSecondCurrency())) {
      spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ExternalSchemes.bloombergTickerSecurityId(currencyPair.getFirstCurrency().getCode()
          + currencyPair.getSecondCurrency().getCode() + " Curncy"));
    } else {
      spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ExternalSchemes.bloombergTickerSecurityId(currencyPair.getSecondCurrency().getCode()
          + currencyPair.getFirstCurrency().getCode() + " Curncy"));
    }
    return ImmutableSet.of(spotRequirement);
  }

}
