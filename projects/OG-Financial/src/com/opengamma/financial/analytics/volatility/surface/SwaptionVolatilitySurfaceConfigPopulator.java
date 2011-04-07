/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SwaptionVolatilitySurfaceConfigPopulator {
  
  public SwaptionVolatilitySurfaceConfigPopulator(ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }
  
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster);
    populateVolatilitySurfaceDefinitions(configMaster);
    return configMaster;
  }

  /**
   * @param definitionConfigMaster
   */
  private static void populateVolatilitySurfaceDefinitions(ConfigMaster configMaster) {
    Tenor[] timeToExpiry = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
                                         Tenor.ofMonths(18), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                         Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                         Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30) };
    Tenor[] swapLength = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                       Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                       Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30) };    
    VolatilitySurfaceDefinition<Tenor, Tenor> us = new VolatilitySurfaceDefinition<Tenor, Tenor>("DEFAULT", Currency.USD,
                                                                                                 "DUNNO", timeToExpiry, swapLength);

    configMaster.add(makeConfigDocument(us));
    
    Tenor[] timeToExpiryTest = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3) };
    Tenor[] swapLengthTest = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2) };
    VolatilitySurfaceDefinition<Tenor, Tenor> test = new VolatilitySurfaceDefinition<Tenor, Tenor>("TEST", Currency.USD,
        "DUNNO", timeToExpiryTest, swapLengthTest);
    configMaster.add(makeConfigDocument(test));
  }
  
  private static ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> makeConfigDocument(VolatilitySurfaceDefinition<Tenor, Tenor> definition) {
    ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>>(VolatilitySurfaceDefinition.class);
    configDocument.setName(definition.getName() + "_" + definition.getCurrency().getCode());
    configDocument.setValue(definition);
    return configDocument;
  }
  
  private static ConfigDocument<VolatilitySurfaceSpecification> makeConfigDocument(VolatilitySurfaceSpecification specification) {
    ConfigDocument<VolatilitySurfaceSpecification> configDocument = new ConfigDocument<VolatilitySurfaceSpecification>(VolatilitySurfaceSpecification.class);
    configDocument.setName(specification.getName() + "_" + specification.getCurrency().getCode());
    configDocument.setValue(specification);
    return configDocument;
  }

  /**
   * @param specConfigMaster
   */
  private static void populateVolatilitySurfaceSpecifications(ConfigMaster configMaster) {
    SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification us = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, surfaceInstrumentProvider);
    configMaster.add(makeConfigDocument(us));
  }
}
