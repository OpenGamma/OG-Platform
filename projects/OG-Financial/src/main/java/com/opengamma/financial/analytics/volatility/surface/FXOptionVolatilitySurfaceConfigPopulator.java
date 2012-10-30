/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FXOptionVolatilitySurfaceConfigPopulator {

  public FXOptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster, UnorderedCurrencyPair.of(Currency.EUR, Currency.USD), "EURUSD");
    populateVolatilitySurfaceDefinitions(configMaster, UnorderedCurrencyPair.of(Currency.EUR, Currency.USD));
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, final UniqueIdentifiable target) {
    final Tenor[] expiryTenors = new Tenor[] {Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
        Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
        Tenor.ofYears(5), Tenor.ofYears(10)};
    @SuppressWarnings("unchecked")
    final Pair<Number, FXVolQuoteType>[] deltaAndTypes = new Pair[] {Pair.of(25, FXVolQuoteType.BUTTERFLY), Pair.of(25, FXVolQuoteType.RISK_REVERSAL),
      Pair.of(15, FXVolQuoteType.BUTTERFLY), Pair.of(15, FXVolQuoteType.RISK_REVERSAL),
      Pair.of(0, FXVolQuoteType.ATM)};
    final VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>> volSurfaceDefinition =
        new VolatilitySurfaceDefinition<Tenor, Pair<Number, FXVolQuoteType>>("DEFAULT_EURUSD_" + InstrumentTypeProperties.FOREX, target, expiryTenors, deltaAndTypes);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(volSurfaceDefinition));
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

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, final UniqueIdentifiable target, final String currencyCrossString) {
    final SurfaceInstrumentProvider<Tenor, Pair<Number, FXVolQuoteType>> surfaceInstrumentProvider = new BloombergFXOptionVolatilitySurfaceInstrumentProvider(currencyCrossString, "Curncy",
        MarketDataRequirementNames.MARKET_VALUE);
    final VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT_EURUSD_" + InstrumentTypeProperties.FOREX, target,
        SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(spec));
  }
}
