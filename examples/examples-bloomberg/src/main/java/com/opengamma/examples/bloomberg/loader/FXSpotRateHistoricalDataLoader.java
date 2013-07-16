/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Finds the historical FX spot time series that need to be loaded. These rates are found by
 * looking at the currencies of the trades in {@link ExampleFxForwardPortfolioLoader} and
 * {@link ExampleVanillaFxOptionPortfolioLoader}. For a single currency (e.g. JPY), three
 * time series are loaded: JPY Curncy, USDJPY Curncy and JPYUSD Curncy.
 */
//TODO REVIEW emcleod 12/7/2013: This should look at CurrencyMatrix, but it isn't available from the ToolContext
public class FXSpotRateHistoricalDataLoader {
  /** USD code */
  private static final String USD = "USD";
  /** The postfix */
  private static final String POSTFIX = " Curncy";
  /** The FX spot rate ids */
  private Set<ExternalId> _fxSpotRateExternalIds;
  /**
   * Gets the list of external ids.
   * @return The external ids
   */
  public Set<ExternalId> getFXSpotRateExternalIds() {
    return _fxSpotRateExternalIds;
  }

  /**
   * Runs the loader.
   * @param tools The tool context, not null
   */
  public void run(final ToolContext tools) {
    ArgumentChecker.notNull(tools, "tools");
    final Set<Currency> currencies = new HashSet<>();
    for (final UnorderedCurrencyPair ccys : ExampleFxForwardPortfolioLoader.CCYS) {
      currencies.add(ccys.getFirstCurrency());
      currencies.add(ccys.getSecondCurrency());
    }
    for (final UnorderedCurrencyPair ccys : ExampleVanillaFxOptionPortfolioLoader.CCYS) {
      currencies.add(ccys.getFirstCurrency());
      currencies.add(ccys.getSecondCurrency());
    }
    currencies.remove(Currency.USD);
    _fxSpotRateExternalIds = getFXSpotRateIds(currencies);
  }

  private Set<ExternalId> getFXSpotRateIds(final Set<Currency> currencies) {
    final Set<ExternalId> tickers = new HashSet<>();
    for (final Currency ccy2 : currencies) {
      tickers.add(ExternalSchemes.bloombergTickerSecurityId(ccy2.getCode() + POSTFIX));
      tickers.add(ExternalSchemes.bloombergTickerSecurityId(USD + ccy2.getCode() + POSTFIX));
      tickers.add(ExternalSchemes.bloombergTickerSecurityId(ccy2.getCode() + USD + POSTFIX));
    }
    return tickers;
  }

}
