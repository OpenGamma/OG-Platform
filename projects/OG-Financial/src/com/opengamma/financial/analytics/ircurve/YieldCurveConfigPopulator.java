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
    final YieldCurveDefinition audDiscounting = CurveDefinitionAndSpecifications.buildSecondaryDiscountingAUDCurveDefinition();
    final YieldCurveDefinition audForwardBasis3M = CurveDefinitionAndSpecifications.buildSecondaryForward3MBasisAUDCurveDefinition();
    final YieldCurveDefinition audForwardBasis6M = CurveDefinitionAndSpecifications.buildSecondaryForward6MBasisAUDCurveDefinition();
    final Currency ccy = Currency.AUD;
    ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
    document.setName("Discounting_" + ccy.getCode());
    document.setValue(audDiscounting);
    ConfigMasterUtils.storeByName(configMaster, document);
    dumpDefinition(audDiscounting);
    document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
    document.setName("ForwardBasis3M_" + ccy.getCode());
    document.setValue(audForwardBasis3M);
    ConfigMasterUtils.storeByName(configMaster, document);
    dumpDefinition(audForwardBasis3M);
    document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
    document.setName("ForwardBasis6M_" + ccy.getCode());
    document.setValue(audForwardBasis6M);
    ConfigMasterUtils.storeByName(configMaster, document);
    dumpDefinition(audForwardBasis6M);
    final YieldCurveDefinition audSingle = CurveDefinitionAndSpecifications.buildSecondarySingleAUDCurveDefinition();
    document = new ConfigDocument<YieldCurveDefinition>(YieldCurveDefinition.class);
    document.setName("Single_" + ccy.getCode());
    document.setValue(audSingle);
    ConfigMasterUtils.storeByName(configMaster, document);
    dumpDefinition(audSingle);

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
    final Currency ccy = Currency.AUD;
    final CurveSpecificationBuilderConfiguration audCurveSpec3M = CurveDefinitionAndSpecifications.buildSyntheticAUD3MCurveSpecification();
    final CurveSpecificationBuilderConfiguration audCurveSpec6M = CurveDefinitionAndSpecifications.buildSyntheticAUD6MCurveSpecification();
    ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(CurveSpecificationBuilderConfiguration.class);
    doc.setName("SECONDARY_3M_" + ccy.getCode());
    doc.setValue(audCurveSpec3M);
    ConfigMasterUtils.storeByName(configMaster, doc);
    doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>(CurveSpecificationBuilderConfiguration.class);
    doc.setName("SECONDARY_6M_" + ccy.getCode());
    doc.setValue(audCurveSpec6M);
    ConfigMasterUtils.storeByName(configMaster, doc);
  }

}
