/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXForwardCurveConfigPopulator {

  public FXForwardCurveConfigPopulator(final ConfigMaster configMaster) {
    populateFXForwardCurveConfigMaster(configMaster);
  }

  public static ConfigMaster populateFXForwardCurveConfigMaster(final ConfigMaster configMaster) {
    populateCurveSpecifications(configMaster, UnorderedCurrencyPair.of(Currency.EUR, Currency.USD), "EUR");
    populateCurveDefinitions(configMaster, UnorderedCurrencyPair.of(Currency.EUR, Currency.USD));
    return configMaster;
  }

  private static void populateCurveDefinitions(final ConfigMaster configMaster, final UnorderedCurrencyPair target) {
    final Tenor[] expiryTenors = new Tenor[] {Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
        Tenor.ofMonths(3), Tenor.ofMonths(6), Tenor.ofMonths(9), Tenor.ofMonths(12),
        Tenor.ofYears(5), Tenor.ofYears(10)};
    final FXForwardCurveDefinition definition = new FXForwardCurveDefinition("DEFAULT_FX_FORWARD", target, expiryTenors);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(definition));
  }

  private static void populateCurveSpecifications(final ConfigMaster configMaster, final UnorderedCurrencyPair target, final String currencyString) {
    final FXForwardCurveInstrumentProvider curveInstrumentProvider = new BloombergFXForwardCurveInstrumentProvider(currencyString, "Curncy",
        MarketDataRequirementNames.MARKET_VALUE);
    final FXForwardCurveSpecification spec = new FXForwardCurveSpecification("DEFAULT_FX_FORWARD", target, curveInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(spec));
  }

  private static ConfigDocument<FXForwardCurveDefinition> makeConfigDocument(final FXForwardCurveDefinition definition) {
    final ConfigDocument<FXForwardCurveDefinition> configDocument = new ConfigDocument<FXForwardCurveDefinition>(FXForwardCurveDefinition.class);
    configDocument.setName(definition.getName());
    configDocument.setValue(definition);
    return configDocument;
  }

  private static ConfigDocument<FXForwardCurveSpecification> makeConfigDocument(final FXForwardCurveSpecification specification) {
    final ConfigDocument<FXForwardCurveSpecification> configDocument = new ConfigDocument<FXForwardCurveSpecification>(FXForwardCurveSpecification.class);
    configDocument.setName(specification.getName());
    configDocument.setValue(specification);
    return configDocument;
  }
}
