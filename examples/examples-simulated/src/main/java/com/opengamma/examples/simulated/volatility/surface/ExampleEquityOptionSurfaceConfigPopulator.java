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
 * Populates the example database with equity option volatility surface definitions and specifications
 * for DJX options.
 * <p>
 * The specifications have expiry date as the x axis and strike as the y axis, and use direct volatility
 * quotes. The instrument provider is {@link ExampleEquityOptionVolatilitySurfaceInstrumentProvider}.
 * <p>
 * The definitions have x axis dates of (2011-01-16, 2011-08-20, 2011-09-17, 2011-12-17, 2012-03-17, 2012-06-16,
 * 2012-12-22, 2013-06-22) and y axis strikes of between 50 and 150 inclusive with an increment of 5.
 */
public class ExampleEquityOptionSurfaceConfigPopulator {

  /**
   * @param configMaster The config master, not null
   */
  public ExampleEquityOptionSurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  /**
   * Populates the config master with a DJX equity option surface definition and specification.
   * @param configMaster The config master, not null
   * @return A populated config master
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster);
    populateVolatilitySurfaceDefinitions(configMaster);
    return configMaster;
  }

  /**
   * Populates the config master with volatility surface definitions.
   * @param configMaster The config master
   */
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
    final VolatilitySurfaceDefinition<LocalDate, Double> usVolSurfaceDefinition = new VolatilitySurfaceDefinition<>(
        "SECONDARY_EQUITY_OPTION", UniqueId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), "DJX_IDX"), equityOptionExpiries, strikes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }

  /**
   * Creates a volatility surface definition config item.
   * @param definition The definition
   * @return The config item
   */
  private static ConfigItem<VolatilitySurfaceDefinition<LocalDate, Double>> makeConfigDocument(final VolatilitySurfaceDefinition<LocalDate, Double> definition) {
    return ConfigItem.of(definition, definition.getName(), VolatilitySurfaceDefinition.class);
  }

  /**
   * Creates a volatility surface specification config item.
   * @param specification The specification
   * @return The config item
   */
  private static ConfigItem<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    return ConfigItem.of(specification, specification.getName(), VolatilitySurfaceSpecification.class);
  }

  /**
   * Populates the config master with volatility surface specifications.
   * @param configMaster The config master
   */
  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final SurfaceInstrumentProvider<LocalDate, Double> surfaceInstrumentProvider =
        new ExampleEquityOptionVolatilitySurfaceInstrumentProvider("DJX_IDX", "EQOPTIONVOL", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    final VolatilitySurfaceSpecification usVolSurfaceDefinition = new VolatilitySurfaceSpecification("SECONDARY_EQUITY_OPTION",
        UniqueId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), "DJX_IDX"), SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(usVolSurfaceDefinition));
  }
}
