/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.surface.SurfaceDefinition;
import com.opengamma.financial.analytics.surface.SurfaceSpecification;
import com.opengamma.financial.analytics.volatility.SurfaceQuoteType;
import com.opengamma.financial.analytics.volatility.VolatilityQuoteUnits;
import com.opengamma.financial.analytics.volatility.surface.ExampleForwardSwapSurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Populates the example database with forward swap rate surface definitions and specifications
 * for a given list of countries.
 * <p>
 * The specifications have expiry as the x axis and maturity as the y axis. The instrument provider is
 * {@link ExampleForwardSwapSurfaceInstrumentProvider}.
 * <p>
 * The definitions have x axis tenors of (3m, 6m, 1y, 2y, 3y, 4y, 5y, 10y, 15y, 20y, 30y) and
 * y axis tenors of (3m, 1y, 2y, 5y, 10y, 15y, 20y, 30y).
 */
public class ExampleForwardSwapSurfaceConfigPopulator {
  /** The expiries */
  private static final Tenor[] EXPIRIES = new Tenor[] {Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
    Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS, Tenor.ofYears(15), Tenor.ofYears(20),
    Tenor.ofYears(30) };
  /** The maturities */
  private static final Tenor[] MATURITIES = new Tenor[] {Tenor.THREE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS,
    Tenor.TEN_YEARS, Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30) };

  /**
   * Populates the config master with forward swap surfaces for a number of countries.
   * @param configMaster The config master, not null
   * @param countries The currency / name pairs, not null
   * @return The populated config master
   */
  public static ConfigMaster populateSurfaceConfigMaster(final ConfigMaster configMaster, final Map<String, String> countries) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(countries, "currencies");
    for (final Map.Entry<String, String> entry : countries.entrySet()) {
      final String country = entry.getKey();
      final String name = entry.getValue();
      final String fullName = country + " " + name;
      populateSurfaceSpecifications(configMaster, fullName, country);
      populateSurfaceDefinitions(configMaster, fullName);
    }
    return configMaster;
  }

  /**
   * Creates and stores the surface specifications.
   * @param configMaster The config master
   * @param name The configuration name
   * @param country The country
   */
  private static void populateSurfaceSpecifications(final ConfigMaster configMaster, final String name, final String country) {
    final SurfaceInstrumentProvider<Tenor, Tenor> instrumentProvider = new ExampleForwardSwapSurfaceInstrumentProvider(country);
    final SurfaceSpecification spec = new SurfaceSpecification(name, SurfaceQuoteType.EXPIRY_MATURITY.getName(),
        VolatilityQuoteUnits.DECIMALS.getName(), instrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(spec, name));
  }

  /**
   * Creates and stores the surface definitions.
   * @param configMaster The config master
   * @param name The configuration name
   */
  private static void populateSurfaceDefinitions(final ConfigMaster configMaster, final String name) {
    final SurfaceDefinition<Tenor, Tenor> def = new SurfaceDefinition<>(name, EXPIRIES, MATURITIES);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(def, name));
  }

  /**
   * Creates a config item.
   * @param config The config object
   * @param name The config name
   * @return The config item
   */
  private static <T> ConfigItem<T> makeConfigDocument(final T config, final String name) {
    return ConfigItem.of(config, name, config.getClass());
  }

}
