/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Populates the example database with FX option volatility surface definitions and specifications.
 */
public class ExampleFXOptionVolatilitySurfaceConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";

  /**
   * @param configMaster The configuration master, not null
   * @param ccyPairs The currency pairs, not null
   */
  public ExampleFXOptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster, final UnorderedCurrencyPair[] ccyPairs) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(ccyPairs, "currency pairs");
    populateVolatilitySurfaceConfigMaster(configMaster, ccyPairs);
  }

  /**
   * Populates the configuration master with volatility definitions and specifications.
   * @param configMaster The configuration master, not null
   * @param ccyPairs The currency pairs, not null
   * @return The configuration master populated with surface definitions and specifications
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster, final UnorderedCurrencyPair[] ccyPairs) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(ccyPairs, "currency pairs");
    for (final UnorderedCurrencyPair pair : ccyPairs) {
      populateVolatilitySurfaceSpecifications(configMaster, pair, "DEFAULT");
      populateVolatilitySurfaceDefinitions(configMaster, pair, "DEFAULT");
    }
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final Tenor[] expiryTenors = new Tenor[] {Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
        Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
        Tenor.ofYears(5), Tenor.ofYears(10)};
    @SuppressWarnings("unchecked")
    final Pair<Number, FXVolQuoteType>[] deltaAndTypes = new Pair[] {
      Pair.of(25, FXVolQuoteType.BUTTERFLY),
      Pair.of(25, FXVolQuoteType.RISK_REVERSAL),
      Pair.of(15, FXVolQuoteType.BUTTERFLY),
      Pair.of(15, FXVolQuoteType.RISK_REVERSAL),
      Pair.of(0, FXVolQuoteType.ATM)};
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.FOREX;
    final VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>> volSurfaceDefinition =
        new VolatilitySurfaceDefinition<>(fullName, target, expiryTenors, deltaAndTypes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(volSurfaceDefinition));
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> surfaceInstrumentProvider = new ExampleFXOptionVolatilitySurfaceInstrumentProvider(target.toString(), "FXVOL",
        MarketDataRequirementNames.MARKET_VALUE);
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.FOREX;
    final VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification(fullName, target, SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(spec));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>>>
  makeConfigDocument(final VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>> definition) {
    return ConfigItem.of(definition, definition.getName(), VolatilitySurfaceDefinition.class);
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    return ConfigItem.of(specification, specification.getName(), VolatilitySurfaceSpecification.class);
  }

}
