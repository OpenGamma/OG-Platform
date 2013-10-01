/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Populates the {@link ConfigMaster} with FX forward curve definitions and specifications.
 */
public class FXForwardCurveConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";
  /** The instrument type name */
  private static final String INSTRUMENT_TYPE = "FX_FORWARD";
  /** Tenors for non-JPY instruments */
  private static final ImmutableList<Tenor> TENORS = ImmutableList.of(Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
    Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofMonths(12),
    Tenor.ofYears(5), Tenor.ofYears(10));

  /**
   * @param configMaster The configuration master, not null
   */
  public FXForwardCurveConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    populateFXForwardCurveConfigMaster(configMaster);
  }

  /**
   * Populates the configuration master with a single EUR/USD FX forward curve definition and specification called DEFAULT.
   * @param configMaster The configuration master, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateFXForwardCurveConfigMaster(final ConfigMaster configMaster) {
    return populateFXForwardCurveConfigMaster(configMaster, Collections.singletonMap(UnorderedCurrencyPair.of(Currency.EUR, Currency.USD),
        new Triple<>("DEFAULT", "EUR", "EUR")));
  }

  /**
   * Populates the configuration master curves.
   * @param configMaster The configuration master, not null
   * @param pairs The currency pairs and (surface name, forward prefix and spot prefix) information, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateFXForwardCurveConfigMaster(final ConfigMaster configMaster, final Map<UnorderedCurrencyPair, Triple<String, String, String>> pairs) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(pairs, "pairs, names and tickers");
    for (final Map.Entry<UnorderedCurrencyPair, Triple<String, String, String>> pair : pairs.entrySet()) {
      populateCurveSpecifications(configMaster, pair.getKey(), pair.getValue());
      populateCurveDefinitions(configMaster, pair.getKey(), pair.getValue().getFirst());
    }
    return configMaster;
  }

  private static void populateCurveDefinitions(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + INSTRUMENT_TYPE;
    final FXForwardCurveDefinition definition = FXForwardCurveDefinition.of(fullName, target, TENORS);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(definition));
  }

  private static void populateCurveSpecifications(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final Triple<String, String, String> triple) {
    final FXForwardCurveInstrumentProvider curveInstrumentProvider = new BloombergFXForwardCurveInstrumentProvider(triple.getSecond(), "Curncy", triple.getThird(),
        MarketDataRequirementNames.MARKET_VALUE);
    final String fullName = triple.getFirst() + SEPARATOR + target.toString() + SEPARATOR + INSTRUMENT_TYPE;
    final FXForwardCurveSpecification spec = new FXForwardCurveSpecification(fullName, target, curveInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(spec));
  }

  private static ConfigItem<FXForwardCurveDefinition> makeConfig(final FXForwardCurveDefinition definition) {
    final ConfigItem<FXForwardCurveDefinition> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<FXForwardCurveSpecification> makeConfig(final FXForwardCurveSpecification specification) {
    final ConfigItem<FXForwardCurveSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());
    return config;
  }
}
