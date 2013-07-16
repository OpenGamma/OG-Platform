/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates the {@link ConfigMaster} with swaption ATM volatility surface definitions and specifications
 * for use with Bloomberg.
 */
public class SwaptionVolatilitySurfaceConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";
  /** The swaption expiries */
  private static final Tenor[] EXPIRIES = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
    Tenor.ofMonths(18), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
    Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
    Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};
  /** The swap maturities */
  private static final Tenor[] MATURITIES = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
    Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
    Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};

  /**
   * @param configMaster The configuration master, not null
   */
  public SwaptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  /**
   * Populates the configuration master with a single USD surface definition and specification called DEFAULT.
   * @param configMaster The configuration master, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    return populateVolatilitySurfaceConfigMaster(configMaster, Collections.singletonMap(Currency.USD, "DEFAULT"));
  }

  /**
   * Populates the configuration master with surfaces.
   * @param configMaster The configuration master, not null
   * @param currencyAndNames A map of currencies to surface names, not null
   * @return The populated configuration master
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster, final Map<Currency, String> currencyAndNames) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(currencyAndNames, "currencies and names");
    for (final Map.Entry<Currency, String> entry : currencyAndNames.entrySet()) {
      populateVolatilitySurfaceSpecifications(configMaster, entry.getKey(), entry.getValue());
      populateVolatilitySurfaceDefinitions(configMaster, entry.getKey(), entry.getValue());
    }
    return configMaster;
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, final Currency currency, final String name) {
    final String fullName = name + SEPARATOR + currency.getCode() + SEPARATOR + InstrumentTypeProperties.SWAPTION_ATM;
    final SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", false, true, " Curncy",
        MarketDataRequirementNames.MARKET_VALUE, ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
    final VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification(fullName, Currency.USD, SurfaceAndCubeQuoteType.EXPIRY_MATURITY_ATM, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(spec));
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, final Currency currency, final String name) {
    final String fullName = name + SEPARATOR + currency.getCode() + SEPARATOR + InstrumentTypeProperties.SWAPTION_ATM;
    final VolatilitySurfaceDefinition<Tenor, Tenor> spec = new VolatilitySurfaceDefinition<>(fullName, Currency.USD, EXPIRIES, MATURITIES);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(spec));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<Tenor, Tenor>> makeConfig(final VolatilitySurfaceDefinition<Tenor, Tenor> definition) {
    final ConfigItem<VolatilitySurfaceDefinition<Tenor, Tenor>> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfig(final VolatilitySurfaceSpecification specification) {
    final ConfigItem<VolatilitySurfaceSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());
    return config;
  }
}
