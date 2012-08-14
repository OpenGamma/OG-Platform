/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BloombergFXSpotRateHistoricalTimeSeriesFunction extends AbstractFXSpotRateHistoricalTimeSeriesFunction {

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency securityCurrency = FinancialSecurityUtils.getCurrency(security);
    final Set<String> resultCurrencies = constraints.getValues(ValuePropertyNames.CURRENCY);
    if (resultCurrencies != null && resultCurrencies.size() == 1) {
      final Currency desiredCurrency = Currency.of(Iterables.getOnlyElement(resultCurrencies));
      if (desiredCurrency.equals(securityCurrency)) {
        return null;
      }
      final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
      final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(desiredCurrency, securityCurrency);
      if (baseQuotePair == null) {
        throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + desiredCurrency + ", " + securityCurrency + ")");
      }
      final ExternalId externalId = getBBGId(baseQuotePair);
      final HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(ExternalIdBundle.of(externalId), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (resolutionResult == null) {
        return null;
      }
      final ValueRequirement tsRequirement =
          HistoricalTimeSeriesFunctionUtils.createHTSRequirement(resolutionResult, MarketDataRequirementNames.MARKET_VALUE, DateConstraint.EARLIEST_START, true, DateConstraint.VALUATION_TIME, true);
      return Collections.singleton(tsRequirement);
    }
    return null;
  }

  private ExternalId getBBGId(final CurrencyPair baseQuotePair) {
    return ExternalSchemes.bloombergTickerSecurityId(baseQuotePair.getBase().getCode() + baseQuotePair.getCounter().getCode() + " Curncy");
  }
}
