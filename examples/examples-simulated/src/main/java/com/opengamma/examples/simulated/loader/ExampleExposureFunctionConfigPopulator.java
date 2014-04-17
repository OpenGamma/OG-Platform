/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Populates the config master with example exposure functions.
 */
public class ExampleExposureFunctionConfigPopulator {

  /**
   * Populates a config master with exposure functions.
   * @param configMaster The config master, not null
   */
  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    String name = "Exposures";
    List<String> exposureFunctionNames = Arrays.asList("Currency");
    Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "USD"), "Default USD Curves");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "CHF"), "CHF FX Implied Curve");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "EUR"), "EUR FX Implied Curve");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "JPY"), "JPY FX Implied Curve");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "GBP"), "GBP FX Implied Curve");
    ExposureFunctions exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
    name = "Bond Exposures";
    exposureFunctionNames = Arrays.asList("Currency");
    idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "UGX"), "UG Government Bond Configuration");
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "USD"), "Default USD Curves");
    exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  /**
   * Creates a config item from an exposure functions configuration object.
   * @param exposureFunctions The exposure functions
   * @return The config item
   */
  private static ConfigItem<ExposureFunctions> makeConfig(final ExposureFunctions exposureFunctions) {
    final ConfigItem<ExposureFunctions> config = ConfigItem.of(exposureFunctions);
    config.setName(exposureFunctions.getName());
    return config;
  }
}
