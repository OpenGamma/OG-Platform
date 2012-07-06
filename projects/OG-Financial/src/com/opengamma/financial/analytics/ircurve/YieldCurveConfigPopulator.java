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
    this(cfgMaster, false);
  }

  public YieldCurveConfigPopulator(final ConfigMaster cfgMaster, final boolean syntheticOnly) {
    populateSyntheticCurveConfigMaster(cfgMaster);
    if (!syntheticOnly) {
      populateCurveConfigMaster(cfgMaster);
    }
  }

  public static ConfigMaster populateCurveConfigMaster(final ConfigMaster cfgMaster) {
    populateCurveDefinitionConfigMaster(cfgMaster);
    populateCurveSpecificationBuilderConfigMaster(cfgMaster);
    return cfgMaster;
  }

  public static ConfigMaster populateSyntheticCurveConfigMaster(final ConfigMaster cfgMaster) {
    populateCurveDefinitionConfigMaster(cfgMaster);
    populateSyntheticCurveSpecificationBuilderConfigMaster(cfgMaster);
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
    final Map<String, Map<Currency, YieldCurveDefinition>> standardCurveDefinitions = CurveDefinitionAndSpecifications.buildOldCurveDefinitions();
    for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : standardCurveDefinitions.entrySet()) {
      final String curveName = entry.getKey();
      final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
      for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
        final Currency ccy = currencyEntry.getKey();
        final YieldCurveDefinition definition = currencyEntry.getValue();
        final ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
        document.setName(curveName + "_" + ccy.getCode());
        document.setValue(definition);
        ConfigMasterUtils.storeByName(configMaster, document);
        dumpDefinition(definition);
      }
    }
    final Map<String, Map<Currency, YieldCurveDefinition>> newCurveDefinitions = CurveDefinitionAndSpecifications.buildNewCurveDefinitions();
    for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : newCurveDefinitions.entrySet()) {
      final String curveName = entry.getKey();
      final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
      for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
        final Currency ccy = currencyEntry.getKey();
        final YieldCurveDefinition definition = currencyEntry.getValue();
        final ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
        document.setName(curveName + "_" + ccy.getCode());
        document.setValue(definition);
        ConfigMasterUtils.storeByName(configMaster, document);
        dumpDefinition(definition);
      }
    }
    final Map<String, Map<Currency, YieldCurveDefinition>> secondaryCurveDefinitions = SecondaryCurveDefinitionAndSpecifications.buildSecondaryCurveDefinitions();
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
    //TODO fix me
    CurveDefinitionAndSpecifications.buildSecondaryDiscountingAUDCurveDefinition();
  }

  public static void populateCurveSpecificationBuilderConfigMaster(final ConfigMaster configMaster) {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = CurveDefinitionAndSpecifications.buildStandardCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : configurations.entrySet()) {
      final ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(
          CurveSpecificationBuilderConfiguration.class);
      doc.setName("DEFAULT_" + entry.getKey().getCode());
      doc.setValue(entry.getValue());
      ConfigMasterUtils.storeByName(configMaster, doc);
    }
  }

  public static void populateSyntheticCurveSpecificationBuilderConfigMaster(final ConfigMaster configMaster) {
    final Map<Currency, CurveSpecificationBuilderConfiguration> syntheticConfigurations = CurveDefinitionAndSpecifications.buildSyntheticCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : syntheticConfigurations.entrySet()) {
      final ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(
          CurveSpecificationBuilderConfiguration.class);
      doc.setName("SECONDARY_" + entry.getKey().getCode());
      doc.setValue(entry.getValue());
      ConfigMasterUtils.storeByName(configMaster, doc);
    }

  }

}
