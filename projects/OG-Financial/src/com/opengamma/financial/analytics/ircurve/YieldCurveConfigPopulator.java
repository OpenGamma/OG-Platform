/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;

/**
 * Populates the yield curve configuration.
 */
public class YieldCurveConfigPopulator {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveConfigPopulator.class);

  public YieldCurveConfigPopulator(final ConfigMaster cfgMaster) {
    populateCurveConfigMaster(cfgMaster);
  }

  public static ConfigMaster populateCurveConfigMaster(final ConfigMaster cfgMaster) {
    populateCurveDefinitionConfigMaster(cfgMaster);
    populateCurveSpecificationBuilderConfigMaster(cfgMaster);
    return cfgMaster;
  }

  private static void dumpDefinition(final YieldCurveDefinition curveDefinition) {
    s_logger.debug("Curve Definition");
    s_logger.debug("  Name:" + curveDefinition.getName());
    s_logger.debug("  Currency:" + curveDefinition.getCurrency());
    s_logger.debug("  Strips:");
    for (final FixedIncomeStrip strip : curveDefinition.getStrips()) {
      s_logger.debug("    " + strip);
    }
  }

  public static void populateCurveDefinitionConfigMaster(final ConfigMaster configMaster) {
    final Map<String, Map<Currency, YieldCurveDefinition>> standardCurveDefinitions = CurveDefinitionAndSpecifications
        .buildStandardCurveDefinitions();
    for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : standardCurveDefinitions.entrySet()) {
      final String curveName = entry.getKey();
      final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
      for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
        final Currency ccy = currencyEntry.getKey();
        final YieldCurveDefinition definition = currencyEntry.getValue();
        final ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>(
            YieldCurveDefinition.class);
        document.setName(curveName + "_" + ccy.getCode());
        document.setValue(definition);
        ConfigMasterUtils.storeByName(configMaster, document);
        dumpDefinition(definition);
      }
    }
    final Map<String, Map<Currency, YieldCurveDefinition>> secondaryCurveDefinitions = SecondaryCurveDefinitionAndSpecifications
        .buildSecondaryCurveDefintions();
    for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : secondaryCurveDefinitions.entrySet()) {
      final String curveName = entry.getKey();
      final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
      for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
        final Currency ccy = currencyEntry.getKey();
        final YieldCurveDefinition definition = currencyEntry.getValue();
        final ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>(
            YieldCurveDefinition.class);
        document.setName(curveName + "_" + ccy.getCode());
        document.setValue(definition);
        ConfigMasterUtils.storeByName(configMaster, document);
        dumpDefinition(definition);
      }
    }
    final ConfigDocument<YieldCurveDefinition> forwardUSD = new ConfigDocument<YieldCurveDefinition>(
        YieldCurveDefinition.class);
    final ConfigDocument<YieldCurveDefinition> swapOnlyUSD = new ConfigDocument<YieldCurveDefinition>(
        YieldCurveDefinition.class);
    swapOnlyUSD.setName("SWAP_ONLY_USD");
    swapOnlyUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnlyUSD);
    final ConfigDocument<YieldCurveDefinition> swapOnlyNo3YrUSD = new ConfigDocument<YieldCurveDefinition>(
        YieldCurveDefinition.class);
    swapOnlyNo3YrUSD.setName("SWAP_ONLY_NO3YR_USD");
    swapOnlyNo3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyNo3YrCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnlyNo3YrUSD);
    final ConfigDocument<YieldCurveDefinition> swapOnly3YrUSD = new ConfigDocument<YieldCurveDefinition>(
        YieldCurveDefinition.class);
    swapOnly3YrUSD.setName("SWAP_ONLY_3YR_USD");
    swapOnly3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnly3YrCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnly3YrUSD);
  }

  public static void populateCurveSpecificationBuilderConfigMaster(final ConfigMaster configMaster) {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = CurveDefinitionAndSpecifications
        .buildStandardCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : configurations.entrySet()) {
      final ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(
          CurveSpecificationBuilderConfiguration.class);
      doc.setName("DEFAULT_" + entry.getKey().getCode());
      doc.setValue(entry.getValue());
      ConfigMasterUtils.storeByName(configMaster, doc);
    }
    final Map<Currency, CurveSpecificationBuilderConfiguration> syntheticConfigurations = SecondaryCurveDefinitionAndSpecifications
        .buildSyntheticCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : syntheticConfigurations.entrySet()) {
      final ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(
          CurveSpecificationBuilderConfiguration.class);
      doc.setName("SECONDARY_" + entry.getKey().getCode());
      doc.setValue(entry.getValue());
      ConfigMasterUtils.storeByName(configMaster, doc);
    }

    //    ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(CurveSpecificationBuilderConfiguration.class);
    //    doc.setName("DEFAULT_USD");
    //    doc.setValue(CurveDefinitionAndSpecifications.buildUSDCurveSpecificationBuilderConfiguration());

  }

}
