/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FXOptionVolatilitySurfaceConfigPopulator {

  public FXOptionVolatilitySurfaceConfigPopulator(final ConfigMaster configMaster) {
    populateVolatilitySurfaceConfigMaster(configMaster);
  }

  public static ConfigMaster populateVolatilitySurfaceConfigMaster(final ConfigMaster configMaster) {
    populateVolatilitySurfaceSpecifications(configMaster, "USDJPY", Currency.USD);
    populateVolatilitySurfaceDefinitions(configMaster, "USDJPY", Currency.USD);
    return configMaster;
  }

  private static void populateVolatilitySurfaceDefinitions(final ConfigMaster configMaster, String currencyCrossString, Currency currency) {
    final Tenor[] expiryTenors = new Tenor[] {Tenor.ofDays(7), Tenor.ofDays(14), Tenor.ofDays(21), Tenor.ofMonths(1),
                                             Tenor.ofMonths(2), Tenor.ofMonths(3), Tenor.ofMonths(4), Tenor.ofMonths(5),
                                             Tenor.ofMonths(6), Tenor.ofMonths(7), Tenor.ofMonths(8), Tenor.ofMonths(9),
                                             Tenor.ofMonths(10), Tenor.ofMonths(11), Tenor.ofYears(1), Tenor.ofYears(2),
                                             Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5), Tenor.ofYears(6),
                                             Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(11),
                                             Tenor.ofYears(15), Tenor.ofYears(20)};
    @SuppressWarnings("unchecked")
    final Pair<Integer, FXVolQuoteType>[] deltaAndTypes = new Pair[] {Pair.of(35, FXVolQuoteType.BUTTERFLY), Pair.of(35, FXVolQuoteType.RISK_REVERSAL),
                                                                      Pair.of(25, FXVolQuoteType.BUTTERFLY), Pair.of(25, FXVolQuoteType.RISK_REVERSAL),
                                                                      Pair.of(15, FXVolQuoteType.BUTTERFLY), Pair.of(15, FXVolQuoteType.RISK_REVERSAL),
                                                                      Pair.of(5, FXVolQuoteType.BUTTERFLY), Pair.of(5, FXVolQuoteType.RISK_REVERSAL),
                                                                      Pair.of(0, FXVolQuoteType.ATM)};
    //TODO currency here is nonsense. We really shouldn't be labelling surfaces with currencies at all.
    final VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>> volSurfaceDefinition =
        new VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>>("DEFAULT_" + currencyCrossString, currency, expiryTenors, deltaAndTypes);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(volSurfaceDefinition));
  }

  private static ConfigDocument<VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>>> 
  makeConfigDocument(final VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>> definition) {
    final ConfigDocument<VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>>> configDocument = new ConfigDocument<VolatilitySurfaceDefinition<Tenor, Pair<Integer, FXVolQuoteType>>>(
        VolatilitySurfaceDefinition.class);
    configDocument.setName(definition.getName()); //TODO this is not right - will give the same surface for everything
    configDocument.setValue(definition);
    return configDocument;
  }

  private static ConfigDocument<VolatilitySurfaceSpecification> makeConfigDocument(final VolatilitySurfaceSpecification specification) {
    final ConfigDocument<VolatilitySurfaceSpecification> configDocument = new ConfigDocument<VolatilitySurfaceSpecification>(VolatilitySurfaceSpecification.class);
    configDocument.setName(specification.getName());
    configDocument.setValue(specification);
    return configDocument;
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster, String currencyCrossString, Currency currency) {
    final SurfaceInstrumentProvider<Tenor, Pair<Integer, FXVolQuoteType>> surfaceInstrumentProvider = new BloombergFXOptionVolatilitySurfaceInstrumentProvider(currencyCrossString, "Curncy",
        MarketDataRequirementNames.MARKET_VALUE);
    final VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT_" + currencyCrossString, currency, surfaceInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(spec));
  }
}
