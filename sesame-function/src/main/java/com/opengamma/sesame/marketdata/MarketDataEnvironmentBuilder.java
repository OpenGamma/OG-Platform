/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.sesame.MulticurveBundle;

/**
 * Builder for creating a {@link MarketDataEnvironment}.
 *
 * @param <T> the type used to identify different scenarios in the environment
 */
public class MarketDataEnvironmentBuilder<T> {

  // linked map so the scenarios are stored in insertion order. do we need to offer other ordering strategies?
  /** Builders for the market data bundle for each scenario. */
  private final Map<T, MapMarketDataBundleBuilder> _builders = new LinkedHashMap<>();

  /**
   * Adds a multicurve to the market data environment
   *
   * @param scenarioId ID of the scenario to which the multicurve should be added
   * @param name the multicurve name
   * @param multicurve the multicurve
   * @return this builder
   */
  public MarketDataEnvironmentBuilder<T> addMulticurve(T scenarioId, String name, MulticurveBundle multicurve) {
    bundle(scenarioId).add(MulticurveId.of(name), multicurve);
    return this;
  }


  /**
   * Adds an FX rate to the market data environment
   *
   * @param scenarioId ID of the scenario to which the multicurve should be added
   * @param currencyPair the currency pair
   * @param rate the FX rate for the currency pair
   * @return this builder
   */
  public MarketDataEnvironmentBuilder<T> addFxRate(T scenarioId, CurrencyPair currencyPair, double rate) {
    bundle(scenarioId).add(FxRateId.of(currencyPair), rate);
    return this;
  }

  /**
   * Returns the bundle builder for a scenario, creating it if necessary
   *
   * @param scenarioId ID of the scenario
   * @return the bundle builder for a scenario
   */
  private MapMarketDataBundleBuilder bundle(T scenarioId) {
    MapMarketDataBundleBuilder builder = _builders.get(scenarioId);

    if (builder != null) {
      return builder;
    }
    MapMarketDataBundleBuilder newBuilder = new MapMarketDataBundleBuilder();
    _builders.put(scenarioId, newBuilder);
    return newBuilder;
  }

  /**
   * Builds a market data environment from the data in this builder.
   *
   * @return a market data environment built from the data in this builder
   */
  public MarketDataEnvironment<T> build() {
    // TODO change the value type to MarketDataBundle when the API allows it
    LinkedHashMap<T, MapMarketDataBundle> bundles = new LinkedHashMap<>(_builders.size());

    for (Map.Entry<T, MapMarketDataBundleBuilder> entry : _builders.entrySet()) {
      T scenarioId = entry.getKey();
      MapMarketDataBundleBuilder bundleBuilder = entry.getValue();
      bundles.put(scenarioId, bundleBuilder.build());
    }
    return new MapMarketDataEnvironment<>(bundles);
  }
}
