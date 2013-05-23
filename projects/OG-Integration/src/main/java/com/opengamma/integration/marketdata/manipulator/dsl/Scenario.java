/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;

/**
 *
 */
public class Scenario implements ScenarioBuilder {

  /** Default calculation configuration name. TODO does this exist as a constant somewhere else? */
  private static final String DEFAULT = "Default";
  private final List<Selector> _selectors = Lists.newArrayList();

  public CurveSelector curve() {
    CurveSelector selector = new CurveSelector(DEFAULT);
    _selectors.add(selector);
    return selector;
  }

  @Override
  public CalculationConfigurationSelector calculationConfig(String configName) {
    return new CalculationConfigurationSelector(configName);
  }

  public Set<MarketDataSelector> getMarketDataSelectors() {
    Set<MarketDataSelector> marketDataSelectors = Sets.newHashSet();
    for (Selector selector : _selectors) {
      marketDataSelectors.add(selector.getMarketDataSelector());
    }
    return marketDataSelectors;
  }
}
