/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import org.threeten.bp.LocalDate;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * 
 */
public class ExampleEquityOptionSurfaceConfigPopulator {

  public ExampleEquityOptionSurfaceConfigPopulator(final ConfigMaster configMaster) {
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
    final Double[] strikes = new Double[21];
    int j = 0;
    for (int i = 50; i <= 150; i += 5) {
      strikes[j++] = (double) i;
    }
    final VolatilitySurfaceDefinition<LocalDate, Double> usVolSurfaceDefinition =
        new VolatilitySurfaceDefinition<LocalDate, Double>("SECONDARY_EQUITY_OPTION", UniqueId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), "DJX_IDX"), equityOptionExpiries, strikes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<LocalDate, Double>> makeConfigDocument(final VolatilitySurfaceDefinition<LocalDate, Double> definition) {
    return ConfigItem.of(definition, definition.getName(), VolatilitySurfaceDefinition.class);
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    return ConfigItem.of(specification, specification.getName(), VolatilitySurfaceSpecification.class);
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<LocalDate, Double> surfaceInstrumentProvider =
        new ExampleEquityOptionVolatilitySurfaceInstrumentProvider("DJX_IDX", "EQOPTIONVOL", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    final VolatilitySurfaceSpecification usVolSurfaceDefinition = new VolatilitySurfaceSpecification("SECONDARY_EQUITY_OPTION",
        UniqueId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), "DJX_IDX"), SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }
}
