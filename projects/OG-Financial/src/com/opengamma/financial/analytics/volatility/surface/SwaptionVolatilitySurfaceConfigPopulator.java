/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.common.Currency;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class SwaptionVolatilitySurfaceConfigPopulator {
  
  public SwaptionVolatilitySurfaceConfigPopulator(ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }
  
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(ConfigMaster configMaster) {
    ConfigTypeMaster<VolatilitySurfaceSpecification> specConfigMaster = configMaster.typed(VolatilitySurfaceSpecification.class);
    populateVolatilitySurfaceSpecifications(specConfigMaster);
    @SuppressWarnings("rawtypes")
    ConfigTypeMaster<VolatilitySurfaceDefinition> definitionConfigMaster = configMaster.typed(VolatilitySurfaceDefinition.class);
    populateVolatilitySurfaceDefinitions(definitionConfigMaster);
    return configMaster;
  }

  /**
   * @param definitionConfigMaster
   */
  private static void populateVolatilitySurfaceDefinitions(@SuppressWarnings("rawtypes") ConfigTypeMaster<VolatilitySurfaceDefinition> definitionConfigMaster) {
    @SuppressWarnings("unchecked") // why is this so hard.
    ConfigTypeMaster<VolatilitySurfaceDefinition<Tenor, Tenor>> configMaster = (ConfigTypeMaster<VolatilitySurfaceDefinition<Tenor, Tenor>>) (Object) definitionConfigMaster;
    Tenor[] timeToExpiry = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
                                         Tenor.ofMonths(18), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                         Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                         Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30) };
    Tenor[] swapLength = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
                                       Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10),
                                       Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30) };    
    VolatilitySurfaceDefinition<Tenor, Tenor> us = new VolatilitySurfaceDefinition<Tenor, Tenor>("DEFAULT", Currency.getInstance("USD"),
                                                                                                 "DUNNO", timeToExpiry, swapLength);

    configMaster.add(makeConfigDocument(us));
    
    Tenor[] timeToExpiryTest = new Tenor[] {Tenor.ofMonths(1), Tenor.ofMonths(3) };
    Tenor[] swapLengthTest = new Tenor[] {Tenor.ofYears(1), Tenor.ofYears(2) };
    VolatilitySurfaceDefinition<Tenor, Tenor> test = new VolatilitySurfaceDefinition<Tenor, Tenor>("TEST", Currency.getInstance("USD"),
        "DUNNO", timeToExpiryTest, swapLengthTest);
    configMaster.add(makeConfigDocument(test));
  }
  
  private static ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> makeConfigDocument(VolatilitySurfaceDefinition<Tenor, Tenor> definition) {
    ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Tenor, Tenor>>();
    configDocument.setName(definition.getName() + "_" + definition.getCurrency().getISOCode());
    configDocument.setValue(definition);
    return configDocument;
  }
  
  private static ConfigDocument<VolatilitySurfaceSpecification> makeConfigDocument(VolatilitySurfaceSpecification specification) {
    ConfigDocument<VolatilitySurfaceSpecification> configDocument = new ConfigDocument<VolatilitySurfaceSpecification>();
    configDocument.setName(specification.getName() + "_" + specification.getCurrency().getISOCode());
    configDocument.setValue(specification);
    return configDocument;
  }

  /**
   * @param specConfigMaster
   */
  private static void populateVolatilitySurfaceSpecifications(ConfigTypeMaster<VolatilitySurfaceSpecification> specConfigMaster) {
    SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification us = new VolatilitySurfaceSpecification("DEFAULT", Currency.getInstance("USD"), surfaceInstrumentProvider);
    specConfigMaster.add(makeConfigDocument(us));
  }
}
