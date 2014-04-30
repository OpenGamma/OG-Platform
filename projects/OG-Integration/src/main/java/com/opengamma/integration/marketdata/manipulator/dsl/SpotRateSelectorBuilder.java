/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.financial.currency.CurrencyPair;

/**
 * Enables building of a {@link SpotRateSelector} in the scenario DSL.
 */
public class SpotRateSelectorBuilder {

  /* package */ SpotRateSelectorBuilder(Scenario scenario) {
    _scenario = scenario;
  }

  /** Scenario that the transformation will be added to. */
  private final Scenario _scenario;

  private Set<CurrencyPair> _currencyPairs;

  public SpotRateSelectorBuilder currencyPair(String currencyPair) {
    if (_currencyPairs != null) {
      throw new IllegalStateException("currencyPair can only be called once");
    }
    _currencyPairs = Sets.newHashSet(parse(currencyPair));
    return this;
  }

  public SpotRateSelectorBuilder currencyPairs(String... currencyPairs) {
    if (_currencyPairs != null) {
      throw new IllegalStateException("currencyPair can only be called once");
    }
    _currencyPairs = Sets.newHashSetWithExpectedSize(currencyPairs.length);
    for (String currencyPair : currencyPairs) {
      _currencyPairs.add(parse(currencyPair));
    }
    return this;
  }

  public SpotRateManipulatorBuilder apply() {
    return new SpotRateManipulatorBuilder(_scenario, getSelector());
  }

  /**
   * Parses a string as a currency pair, accepts formats 'EUR/USD' and 'EURUSD'.
   * @param currencyPair A currency pair as a string
   * @return The currency pair
   * @throws IllegalArgumentException If the argument can't be parsed as a currency pair
   */
  /* package */ static CurrencyPair parse(String currencyPair) {
    if (currencyPair.length() == 7) {
      return CurrencyPair.parse(currencyPair);
    } else {
      return CurrencyPair.parse(currencyPair.substring(0, 3) + "/" + currencyPair.substring(3));
    }
  }

  /* package */ SpotRateSelector getSelector() {
    return new SpotRateSelector(_scenario.getCalcConfigNames(), _currencyPairs);
  }

  /* package */ Scenario getScenario() {
    return _scenario;
  }
}
