/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.forex.AbstractFXSpotRateMarketDataFunction;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class ExampleForexSpotRateMarketDataFunction extends AbstractFXSpotRateMarketDataFunction {
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(desiredValue.getTargetSpecification().getUniqueId());
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(currencyPair.getFirstCurrency(), currencyPair.getSecondCurrency());
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + currencyPair.getFirstCurrency() + ", " + currencyPair.getSecondCurrency() + ")");
    }
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, 
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, baseQuotePair.getBase().getCode() + baseQuotePair.getCounter().getCode()));
    return ImmutableSet.of(spotRequirement);
  }

}
