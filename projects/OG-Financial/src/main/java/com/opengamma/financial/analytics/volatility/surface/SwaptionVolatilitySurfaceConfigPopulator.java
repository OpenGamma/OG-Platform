/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SwaptionVolatilitySurfaceConfigPopulator {

  public SwaptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster);
    populateVolatilitySurfaceDefinitions(configMaster);
    return configMaster;
  }

  /**
   * @param configMaster
   */
  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster) {
    final Tenor[] timeToExpiry = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
        Tenor.ofMonths(18), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
        Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
        Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};
    final Tenor[] swapLength = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
        Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
        Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};
    final VolatilitySurfaceDefinition<Tenor, Tenor> us = new VolatilitySurfaceDefinition<Tenor, Tenor>("DEFAULT_USD_SWAPTION", Currency.USD,
        timeToExpiry, swapLength);

    ConfigMasterUtils.storeByName(configMaster, makeConfig(us));
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

  /**
   * @param configMaster
   */
  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", false, true, " Curncy",
        MarketDataRequirementNames.MARKET_VALUE);
    final VolatilitySurfaceSpecification us = new VolatilitySurfaceSpecification("DEFAULT_USD_SWAPTION", Currency.USD,
        SurfaceAndCubeQuoteType.PAY_RECEIVE_DELTA,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(us));
  }
}
