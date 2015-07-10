/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
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
    final VolatilitySurfaceDefinition<Integer, Double> usVolSurfaceDefinition = new VolatilitySurfaceDefinition<Integer, Double>("DEFAULT_USD_IR_FUTURE_OPTION",
        Currency.USD, futureOptionNumbers, strikes);
    final FuturePriceCurveDefinition<Integer> usFuturePriceCurveDefinition = FuturePriceCurveDefinition.of("DEFAULT_USD_IR_FUTURE_PRICE", Currency.USD, futureOptionNumbers);
    final VolatilitySurfaceDefinition<Integer, Double> euVolSurfaceDefinition = new VolatilitySurfaceDefinition<Integer, Double>("DEFAULT_EUR_IR_FUTURE_OPTION",
        Currency.EUR, futureOptionNumbers, strikes);
    final FuturePriceCurveDefinition<Integer> euFuturePriceCurveDefinition = FuturePriceCurveDefinition.of("DEFAULT_EUR_IR_FUTURE_PRICE", Currency.EUR, futureOptionNumbers);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usVolSurfaceDefinition));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usFuturePriceCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(euVolSurfaceDefinition));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(euFuturePriceCurveDefinition));
  }

  private static ConfigItem<VolatilitySurfaceDefinition<Integer, Double>> makeConfig(final VolatilitySurfaceDefinition<Integer, Double> definition) {
    final ConfigItem<VolatilitySurfaceDefinition<Integer, Double>> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<VolatilitySurfaceSpecification> makeConfig(final VolatilitySurfaceSpecification specification) {
    final ConfigItem<VolatilitySurfaceSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());
    return config;
  }

  private static ConfigItem<FuturePriceCurveDefinition<Integer>> makeConfig(final FuturePriceCurveDefinition<Integer> definition) {
    final ConfigItem<FuturePriceCurveDefinition<Integer>> config = ConfigItem.of(definition);
    config.setName(definition.getName());
    return config;
  }

  private static ConfigItem<FuturePriceCurveSpecification> makeConfig(final FuturePriceCurveSpecification specification) {
    final ConfigItem<FuturePriceCurveSpecification> config = ConfigItem.of(specification);
    config.setName(specification.getName());   
    return config;
  }

  private static void populateVolatilitySurfaceSpecifications(final ConfigMaster configMaster) {
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider usSurfaceInstrumentProvider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider("ED", "Comdty",
        MarketDataRequirementNames.IMPLIED_VOLATILITY, 97.775, "CME");
    final FuturePriceCurveInstrumentProvider<Number> usCurveInstrumentProvider = new BloombergIRFuturePriceCurveInstrumentProvider("ED", "Comdty",
        MarketDataRequirementNames.MARKET_VALUE, "BLOOMBERG_TICKER_WEAK");
    final VolatilitySurfaceSpecification usVolSurfaceDefinition = new VolatilitySurfaceSpecification("DEFAULT_USD_IR_FUTURE_OPTION", Currency.USD,
        SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        usSurfaceInstrumentProvider);
    final FuturePriceCurveSpecification usFutureCurveDefinition = new FuturePriceCurveSpecification("DEFAULT_USD_IR_FUTURE_PRICE", Currency.USD, usCurveInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usVolSurfaceDefinition));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(usFutureCurveDefinition));
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider euSurfaceInstrumentProvider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider("ER", "Comdty",
        MarketDataRequirementNames.IMPLIED_VOLATILITY, 97.775, "CME");
    final FuturePriceCurveInstrumentProvider<Number> euCurveInstrumentProvider = new BloombergIRFuturePriceCurveInstrumentProvider("ER", "Comdty",
        MarketDataRequirementNames.MARKET_VALUE, "BLOOMBERG_TICKER_WEAK");
    final VolatilitySurfaceSpecification euVolSurfaceDefinition = new VolatilitySurfaceSpecification("DEFAULT_EUR_IR_FUTURE_OPTION", Currency.EUR,
        SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        euSurfaceInstrumentProvider);
    final FuturePriceCurveSpecification euFutureCurveDefinition = new FuturePriceCurveSpecification("DEFAULT_EUR_IR_FUTURE_PRICE", Currency.EUR, euCurveInstrumentProvider);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(euVolSurfaceDefinition));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(euFutureCurveDefinition));
  }
}
