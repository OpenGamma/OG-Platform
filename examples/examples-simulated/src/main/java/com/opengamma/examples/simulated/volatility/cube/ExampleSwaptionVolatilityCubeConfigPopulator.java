/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.cube;

import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.volatility.CubeQuoteType;
import com.opengamma.financial.analytics.volatility.VolatilityQuoteUnits;
import com.opengamma.financial.analytics.volatility.cube.CubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.ExampleSwaptionVolatilityCubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates the example database with swaption volatility cube definitions and specifications
 * for a given list of currencies.
 * <p>
 * The specifications have expiry as the x axis, maturity as the y axis and relative strike as
 * the z axis, and use direct volatility quotes. The instrument provider is
 * {@link ExampleSwaptionVolatilityCubeInstrumentProvider}.
 * <p>
 * The definitions have x axis tenors of (3m, 6m, 1y, 2y, 3y, 4y, 5y, 10y, 15y, 20y, 30y),
 * y axis tenors of (3m, 1y, 2y, 5y, 10y, 15y, 20y, 30y) and z axis tenors of (-200, -100, 0, 100, 200).
 */
public class ExampleSwaptionVolatilityCubeConfigPopulator {
  /** The expiries */
  private static final Tenor[] EXPIRIES = new Tenor[] {Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
    Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS, Tenor.ofYears(15), Tenor.ofYears(20),
    Tenor.ofYears(30) };
  /** The maturities */
  private static final Tenor[] MATURITIES = new Tenor[] {Tenor.THREE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS,
    Tenor.TEN_YEARS, Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30) };
  /** The relative strikes */
  private static final Double[] RELATIVE_STRIKES = new Double[] {-200., -100., 0., 100., 200. };

  /**
   * Populates the config master with swaption volatility cubes for a number of currencies.
   * @param configMaster The config master, not null
   * @param currencies The currency / name pairs, not null
   * @return The populated config master
   */
  public static ConfigMaster populateVolatilityCubeConfigMaster(final ConfigMaster configMaster, final Map<Currency, String> currencies) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(currencies, "currencies");
    for (final Map.Entry<Currency, String> entry : currencies.entrySet()) {
      final Currency currency = entry.getKey();
      final String name = entry.getValue();
      final String fullName = currency + " " + name;
      populateVolatilityCubeSpecifications(configMaster, fullName, currency.getCode());
      populateVolatilityCubeDefinitions(configMaster, fullName);
    }
    return configMaster;
  }

  /**
   * Creates and stores the volatility cube specifications.
   * @param configMaster The config master
   * @param name The configuration name
   * @param currency The currency
   */
  private static void populateVolatilityCubeSpecifications(final ConfigMaster configMaster, final String name, final String currency) {
    final CubeInstrumentProvider<Tenor, Tenor, Double> instrumentProvider = new ExampleSwaptionVolatilityCubeInstrumentProvider(currency);
    final VolatilityCubeSpecification spec = new VolatilityCubeSpecification(name, CubeQuoteType.EXPIRY_MATURITY_RELATIVE_STRIKE.getName(),
        VolatilityQuoteUnits.LOGNORMAL.getName(), instrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(spec, name));
  }

  /**
   * Creates and stores the volatility cube definitions.
   * @param configMaster The config master
   * @param name The configuration name
   */
  private static void populateVolatilityCubeDefinitions(final ConfigMaster configMaster, final String name) {
    final VolatilityCubeDefinition<Tenor, Tenor, Double> def = new VolatilityCubeDefinition<>(name, CubeQuoteType.EXPIRY_MATURITY_RELATIVE_STRIKE.getName(),
        EXPIRIES, MATURITIES, RELATIVE_STRIKES);
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
