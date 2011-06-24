/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureOptionSurfaceConfigPopulator {

  public InterestRateFutureOptionSurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster);
    populateVolatilitySurfaceDefinitions(configMaster);
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster) {
    final Integer[] futureOptionNumbers = new Integer[18];
    for (int i = 0; i < 18; i++) {
      futureOptionNumbers[i] = i + 1;
    }
    final Double[] strikes = new Double[24];
    double strike = 99.75;
    for (int i = 0; i < 24; i++) {
      strikes[i] = strike;
      strike -= 0.25;
    }
    final VolatilitySurfaceDefinition<Integer, Double> usVolSurfaceDefinition = new VolatilitySurfaceDefinition<Integer, Double>("DEFAULT", Currency.USD, "", futureOptionNumbers, strikes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }

  private static ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>> makeConfigDocument(final VolatilitySurfaceDefinition<Integer, Double> definition) {
    final ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>>(VolatilitySurfaceDefinition.class);
    configDocument.setName(definition.getName() + "_" + definition.getCurrency().getCode());
    configDocument.setValue(definition);
    return configDocument;
  }

  private static ConfigDocument<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    final ConfigDocument<VolatilitySurfaceSpecification> configDocument = new ConfigDocument<VolatilitySurfaceSpecification>(VolatilitySurfaceSpecification.class);
    configDocument.setName(specification.getName() + "_" + specification.getCurrency().getCode());
    configDocument.setValue(specification);
    return configDocument;
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<Integer, Double> surfaceInstrumentProvider = new BloombergInterestRateFutureOptionVolatilitySurfaceInstrumentProvider("ED", "Comdty");
    final VolatilitySurfaceSpecification usVolSurfaceDefinition = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }
}
