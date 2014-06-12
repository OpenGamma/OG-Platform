/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.curve;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * Populates the example database with FX forward curve definitions and specifications.
 */
public class ExampleFXForwardCurveConfigPopulator {
  /** The separator */
  private static final String SEPARATOR = "_";
  /** The instrument type name */
  private static final String INSTRUMENT_TYPE = "FX_FORWARD";
  /** Tenors for non-JPY instruments */
  private static final ImmutableList<Tenor> TENORS = ImmutableList.of(Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
    Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1),
    Tenor.ofYears(5), Tenor.ofYears(10));
  /** Tenors for JPY */
  private static final ImmutableList<Tenor> JPY_TENORS = ImmutableList.of(Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
    Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofYears(1), Tenor.ofYears(2));

  /**
   * @param configMaster The configuration master, not null
   * @param ccyPairs The currency pairs, not null
   */
  public ExampleFXForwardCurveConfigPopulator(final ConfigMaster configMaster, final UnorderedCurrencyPair[] ccyPairs) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(ccyPairs, "currency pairs");
    populateCurveConfigMaster(configMaster, ccyPairs);
  }

  /**
   * Populates the configuration master.
   * @param configMaster The configuration master, not null
   * @param ccyPairs The currency pairs, not null
   * @return A populated configuration master
   */
  public static ConfigMaster populateCurveConfigMaster(final ConfigMaster configMaster, final UnorderedCurrencyPair[] ccyPairs) {
    ArgumentChecker.notNull(configMaster, "configuration master");
    ArgumentChecker.notNull(ccyPairs, "currency pairs");
    for (final UnorderedCurrencyPair pair : ccyPairs) {
      populateCurveSpecifications(configMaster, pair, "DEFAULT");
      populateCurveDefinitions(configMaster, pair, "DEFAULT");
    }
    return configMaster;
  }

  private static void populateCurveSpecifications(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final FXForwardCurveInstrumentProvider curveInstrumentProvider = new ExampleFXForwardCurveInstrumentProvider(target.toString(), "FXFORWARD", target.toString(),
        MarketDataRequirementNames.MARKET_VALUE);
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + INSTRUMENT_TYPE;
    final FXForwardCurveSpecification specification = new FXForwardCurveSpecification(fullName, target, curveInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(specification));
  }

  private static void populateCurveDefinitions(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String name) {
    final String fullName = name + SEPARATOR + target.toString() + SEPARATOR + INSTRUMENT_TYPE;
    final FXForwardCurveDefinition definition;
    if (target.getFirstCurrency().equals(Currency.JPY) || target.getSecondCurrency().equals(Currency.JPY)) {
      definition = FXForwardCurveDefinition.of(fullName, target, JPY_TENORS);
    } else {
      definition = FXForwardCurveDefinition.of(fullName, target, TENORS);
    }
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(definition));
  }

  private static ConfigItem<FXForwardCurveSpecification> makeConfigDocument(final FXForwardCurveSpecification specification) {
    return ConfigItem.of(specification, specification.getName(), FXForwardCurveSpecification.class);
  }

  private static ConfigItem<FXForwardCurveDefinition> makeConfigDocument(final FXForwardCurveDefinition definition) {
    return ConfigItem.of(definition, definition.getName(), FXForwardCurveDefinition.class);
  }
}
