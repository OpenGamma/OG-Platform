/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.threeten.bp.LocalDate;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * 
 */
public class EquityOptionSurfaceConfigPopulator {

  public EquityOptionSurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster);
    populateVolatilitySurfaceDefinitions(configMaster);
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster) {
    final LocalDate[] equityOptionExpiries = new LocalDate[] {LocalDate.of(2011, 7, 16), LocalDate.of(2011, 8, 20),
        LocalDate.of(2011, 9, 17), LocalDate.of(2011, 12, 17),
        LocalDate.of(2012, 3, 17), LocalDate.of(2012, 6, 16),
        LocalDate.of(2012, 12, 22), LocalDate.of(2013, 6, 22) };
    final Double[] strikes = new Double[31];
    int j = 0;
    for (int i = 50; i <= 200; i += 5) {
      strikes[j++] = (double) i;
    }
    final VolatilitySurfaceDefinition<LocalDate, Double> usVolSurfaceDefinition =
        new VolatilitySurfaceDefinition<LocalDate, Double>("DEFAULT_EQUITY_OPTION", UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index"), equityOptionExpiries, strikes);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usVolSurfaceDefinition));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<LocalDate, Double>> makeConfig(final VolatilitySurfaceDefinition<LocalDate, Double> definition) {
    final ConfigItem<VolatilitySurfaceDefinition<LocalDate, Double>> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfig(final VolatilitySurfaceSpecification specification) {
    final ConfigItem<VolatilitySurfaceSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());
    return config;
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<LocalDate, Double> surfaceInstrumentProvider =
        new BloombergEquityOptionVolatilitySurfaceInstrumentProvider("DJX", "Index", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    final VolatilitySurfaceSpecification usVolSurfaceSpec = new VolatilitySurfaceSpecification("DEFAULT_DJX_EQUITY_OPTION",
        UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index"), SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usVolSurfaceSpec));
  }
}
