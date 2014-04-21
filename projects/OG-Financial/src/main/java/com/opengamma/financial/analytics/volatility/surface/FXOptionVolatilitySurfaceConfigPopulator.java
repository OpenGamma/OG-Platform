/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Populates the {@link ConfigMaster} with FX option volatility surface definitions and specifications. The
 * surfaces are quoted in ATM / risk-reversal / butterfly form and uses Bloomberg tickers.
 */
public class FXOptionVolatilitySurfaceConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";
  /** The tenors */
  private static final Tenor[] EXPIRIES = new Tenor[] {Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
    Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
    Tenor.ofYears(5), Tenor.ofYears(10)};
  /** The y-axis */
  @SuppressWarnings("unchecked")
  private static final Pair<Number, FXVolQuoteType>[] YS = new Pair[] {Pairs.of(25, FXVolQuoteType.BUTTERFLY), Pairs.of(25, FXVolQuoteType.RISK_REVERSAL),
    Pairs.of(15, FXVolQuoteType.BUTTERFLY), Pairs.of(15, FXVolQuoteType.RISK_REVERSAL),
    Pairs.of(0, FXVolQuoteType.ATM)};

  /**
   * @param configMaster The configuration master, not null
   */
  public FXOptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  /**
   * Populates the configuration master with a single EUR/USD surface definition and specification called DEFAULT.
   * @param configMaster The configuration master, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    return populateVolatilitySurfaceConfigMaster(configMaster, Collections.singletonMap(UnorderedCurrencyPair.of(Currency.EUR, Currency.USD), "DEFAULT"));
  }

  /**
   * Populates the configuration master with surfaces.
   * @param configMaster The configuration master, not null
   * @param pairsAndNames A map of currency pairs to surface names, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster, final Map<UnorderedCurrencyPair, String> pairsAndNames) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(pairsAndNames, "pairs and names");
    for (final Map.Entry<UnorderedCurrencyPair, String> entry : pairsAndNames.entrySet()) {
      populateVolatilitySurfaceSpecifications(configMaster, entry.getKey(), entry.getValue());
      populateVolatilitySurfaceDefinitions(configMaster, entry.getKey(), entry.getValue());
    }
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.FOREX;
    final VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>> volSurfaceDefinition = new VolatilitySurfaceDefinition<>(fullName, target, EXPIRIES, YS);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(volSurfaceDefinition));
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> surfaceInstrumentProvider = new BloombergFXOptionVolatilitySurfaceInstrumentProvider(target.toString(), "Curncy",
        MarketDataRequirementNames.MARKET_VALUE, ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.FOREX;
    final VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification(fullName, target, SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(spec));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>>> makeConfig(final VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>> definition) {
    final ConfigItem<VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>>> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfig(final VolatilitySurfaceSpecification specification) {
    final ConfigItem<VolatilitySurfaceSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());
    return config;
  }

}
