/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class IRFutureOptionSurfaceConfigPopulator {

  public IRFutureOptionSurfaceConfigPopulator(final ConfigMaster configMaster) {
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
    double strike = 99.875;
    for (int i = 0; i < 24; i++) {
      strikes[i] = strike;
      strike -= 0.125; // quoted option strikes decrease by this amount
    }
    final VolatilitySurfaceDefinition<Integer, Double> usVolSurfaceDefinition = new VolatilitySurfaceDefinition<Integer, Double>("DEFAULT_IR_FUTURE", Currency.USD, futureOptionNumbers, strikes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }

  private static ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>> makeConfigDocument(final VolatilitySurfaceDefinition<Integer, Double> definition) {
    final ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Integer, Double>>(VolatilitySurfaceDefinition.class);
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

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<Number, Double> surfaceInstrumentProvider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider("ED", "Comdty",
        MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 97.775);
    final VolatilitySurfaceSpecification usVolSurfaceDefinition = new VolatilitySurfaceSpecification("DEFAULT_IR_FUTURE", Currency.USD, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }
}
