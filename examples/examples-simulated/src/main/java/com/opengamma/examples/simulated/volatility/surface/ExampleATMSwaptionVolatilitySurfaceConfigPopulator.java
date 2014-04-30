/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates the example database with ATM swaption volatility surface definitions and specifications
 * for a given list of currencies.
 * <p>
 * The specifications have expiry as the x axis and maturity as the y axis, and use direct volatility
 * quotes. The instrument provider is {@link ExampleSwaptionVolatilitySurfaceInstrumentProvider}.
 * <p>
 * The definitions have x axis tenors of (3m, 6m, 1y, 2y, 3y, 5y, 10y) and y axis tenors of
 * (1y, 2y, 3y, 5y, 7y, 10y, 12y, 15y, 20y, 30y).
 */
public class ExampleATMSwaptionVolatilitySurfaceConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";

  /**
   * @param configMaster The configuration master, not null
   * @param currencies The currencies, not null
   */
  public ExampleATMSwaptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster, final Map<Currency, String> currencies) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(currencies, "currencies");
    populateVolatilitySurfaceConfigMaster(configMaster, currencies);
  }

  /**
   * Populates the configuration master with ATM swaption volatility surface definitions and specifications.
   * @param configMaster The configuration master, not null
   * @param currencies The currencies, not null
   * @return A populated configuration master.
   */
  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster, final Map<Currency, String> currencies) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(currencies, "currencies");
    for (final Map.Entry<Currency, String> entry : currencies.entrySet()) {
      final Currency currency = entry.getKey();
      final String name = entry.getValue();
      populateVolatilitySurfaceSpecifications(configMaster, currency, name);
      populateVolatilitySurfaceDefinitions(configMaster, currency, name);
    }
    return configMaster;
  }

  /**
   * Populates ATM swaption volatility surface specifications.
   * @param configMaster The config master
   * @param target The surface target
   * @param name The surface name
   */
  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, final Currency target, final String name) {
    final SurfaceInstrumentProvider<Tenor, Tenor> surfaceInstrumentProvider = new ExampleSwaptionVolatilitySurfaceInstrumentProvider(target.toString(), "ATMSWAPTION", false, true,
        "");
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.SWAPTION_ATM;
    final VolatilitySurfaceSpecification specification = new VolatilitySurfaceSpecification(fullName, target, SurfaceAndCubeQuoteType.EXPIRY_MATURITY_ATM,
        surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(specification));
  }

  /**
   * Populates ATM swaption volatility surface definitions.
   * @param configMaster The config master
   * @param target The surface target
   * @param name The surface name
   */
  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, final Currency target, final String name) {
    final Tenor[] swaptionExpiries = new Tenor[] {Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS,
      Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
    final Tenor[] swapMaturities = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.SEVEN_YEARS,
      Tenor.TEN_YEARS, Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30)};
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + InstrumentTypeProperties.SWAPTION_ATM;
    final VolatilitySurfaceDefinition<Tenor, Tenor> definition = new VolatilitySurfaceDefinition<>(fullName, target, swapMaturities, swaptionExpiries);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(definition));
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
   * Creates a volatility surface definition config item.
   * @param definition The definition
   * @return The config item
   */
  private static ConfigItem<VolatilitySurfaceDefinition<Tenor, Tenor>> makeConfigDocument(final VolatilitySurfaceDefinition<Tenor, Tenor> definition) {
    return ConfigItem.of(definition, definition.getName(), VolatilitySurfaceDefinition.class);
  }
}

