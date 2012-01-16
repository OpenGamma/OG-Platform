/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigDocument;
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
   * @param definitionConfigMaster
   */
  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster) {
    final Tenor[] timeToExpiry = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
                                         Tenor.ofMonths(18), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                         Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                         Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};
    final Tenor[] swapLength = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                       Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                       Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30)};
    final VolatilitySurfaceDefinition<Tenor, Tenor> us = new VolatilitySurfaceDefinition<Tenor, Tenor>("DEFAULT_SWAPTION", Currency.USD,
                                                                                                 timeToExpiry, swapLength);

    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(us));

    final Tenor[] timeToExpiryTest = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3)};
    final Tenor[] swapLengthTest = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2)};
    final VolatilitySurfaceDefinition<Tenor, Tenor> test = new VolatilitySurfaceDefinition<Tenor, Tenor>("TEST", Currency.USD,
        timeToExpiryTest, swapLengthTest);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(test));
  }

  private static ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> makeConfigDocument(final VolatilitySurfaceDefinition<Tenor, Tenor> definition) {
    final ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>>(VolatilitySurfaceDefinition.class);
    configDocument.setName(definition.getName());
    configDocument.setValue(definition);
    return configDocument;
  }

  private static ConfigDocument<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    final ConfigDocument<VolatilitySurfaceSpecification> configDocument = new ConfigDocument<VolatilitySurfaceSpecification>(VolatilitySurfaceSpecification.class);
    configDocument.setName(specification.getName());
    configDocument.setValue(specification);
    return configDocument;
  }

  /**
   * @param specConfigMaster
   */
  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", false, true, " Curncy",
        MarketDataRequirementNames.MARKET_VALUE);
    final VolatilitySurfaceSpecification us = new VolatilitySurfaceSpecification("DEFAULT_SWAPTION", Currency.USD, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(us));
  }
}
