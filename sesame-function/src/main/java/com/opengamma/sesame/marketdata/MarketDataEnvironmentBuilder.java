/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.sesame.MulticurveBundle;

/**
 * Builder for creating a {@link MarketDataEnvironment}.
 */
public class MarketDataEnvironmentBuilder {

  // linked map so the scenarios are stored in insertion order. do we need to offer other ordering strategies?
  /** Builders for the market data bundle for each scenario. */
  private final Map<String, MapMarketDataBundleBuilder> _builders = new LinkedHashMap<>();

  /**
   * Adds a multicurve to the market data environment
   *
   * @param scenarioId ID of the scenario to which the multicurve should be added
   * @param name the multicurve name
   * @param multicurve the multicurve
   * @return this builder
   */
  public MarketDataEnvironmentBuilder addMulticurve(String scenarioId, String name, MulticurveBundle multicurve) {
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
  public MarketDataEnvironmentBuilder addFxRate(String scenarioId, CurrencyPair currencyPair, double rate) {
    bundle(scenarioId).add(FxRateId.of(currencyPair), rate);
    return this;
  }

  /**
   * Sets the valuation time for a scenario.
   *
   * @param scenarioId ID of the scenario
   * @param valuationTime the valuation time for the scenario
   * @return this builder
   */
  public MarketDataEnvironmentBuilder valuationTime(String scenarioId, ZonedDateTime valuationTime) {
    bundle(scenarioId).valuationTime(valuationTime);
    return this;
  }

  /**
   * Returns the bundle builder for a scenario, creating it if necessary
   *
   * @param scenarioId ID of the scenario
   * @return the bundle builder for a scenario
   */
  private MapMarketDataBundleBuilder bundle(String scenarioId) {
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
  public MarketDataEnvironment build() {
    // TODO change the value type to MarketDataBundle when the API allows it
    LinkedHashMap<String, MapMarketDataBundle> bundles = new LinkedHashMap<>(_builders.size());

    for (Map.Entry<String, MapMarketDataBundleBuilder> entry : _builders.entrySet()) {
      String scenarioId = entry.getKey();
      MapMarketDataBundleBuilder bundleBuilder = entry.getValue();
      bundles.put(scenarioId, bundleBuilder.build());
    }
    return new MapMarketDataEnvironment(bundles);
  }
}
