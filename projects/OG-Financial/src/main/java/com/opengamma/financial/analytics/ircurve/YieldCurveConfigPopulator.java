/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
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
    if (syntheticOnly) {
      populateSyntheticCurveConfigMaster(cfgMaster);
    } else {
      populateCurveConfigMaster(cfgMaster);
    }
  }

  public static ConfigMaster populateCurveConfigMaster(final ConfigMaster cfgMaster) {
    populateCurveDefinitionConfigMaster(cfgMaster, false);
    populateCurveSpecificationBuilderConfigMaster(cfgMaster);
    return cfgMaster;
  }

  public static ConfigMaster populateSyntheticCurveConfigMaster(final ConfigMaster cfgMaster) {
    populateCurveDefinitionConfigMaster(cfgMaster, true);
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

  public static void populateCurveDefinitionConfigMaster(final ConfigMaster configMaster, final boolean syntheticOnly) {
    if (syntheticOnly) {
      final Map<String, Map<Currency, YieldCurveDefinition>> newCurveDefinitions = CurveDefinitionAndSpecifications.buildNewCurveDefinitions();
      for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : newCurveDefinitions.entrySet()) {
        final String curveName = entry.getKey();
        final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
        for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
          final Currency ccy = currencyEntry.getKey();
          final YieldCurveDefinition definition = currencyEntry.getValue();
          final ConfigItem<YieldCurveDefinition> item = ConfigItem.of(definition);
          item.setName(curveName + "_" + ccy.getCode());
          ConfigMasterUtils.storeByName(configMaster, item);
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
          final ConfigItem<YieldCurveDefinition> item = ConfigItem.of(definition);
          item.setName(curveName + "_" + ccy.getCode());
          ConfigMasterUtils.storeByName(configMaster, item);
          dumpDefinition(definition);
        }
      }
      final Currency ccy = Currency.AUD;
      final YieldCurveDefinition audDiscounting = CurveDefinitionAndSpecifications.buildSecondaryDiscountingAUDCurveDefinition();
      ConfigItem<YieldCurveDefinition> item = ConfigItem.of(audDiscounting);
      item.setName("Discounting_" + ccy.getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
      dumpDefinition(audDiscounting);

      final YieldCurveDefinition audForwardBasis3M = CurveDefinitionAndSpecifications.buildSecondaryForward3MBasisAUDCurveDefinition();
      item = ConfigItem.of(audForwardBasis3M);
      item.setName("ForwardBasis3M_" + ccy.getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
      dumpDefinition(audForwardBasis3M);

      final YieldCurveDefinition audForwardBasis6M = CurveDefinitionAndSpecifications.buildSecondaryForward6MBasisAUDCurveDefinition();
      item = ConfigItem.of(audForwardBasis6M);
      item.setName("ForwardBasis6M_" + ccy.getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
      dumpDefinition(audForwardBasis6M);

      final YieldCurveDefinition audSingle = CurveDefinitionAndSpecifications.buildSecondarySingleAUDCurveDefinition();
      item = ConfigItem.of(audSingle);
      item.setName("Single_" + ccy.getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
      dumpDefinition(audSingle);
    } else {
      final Map<String, Map<Currency, YieldCurveDefinition>> newCurveDefinitions = CurveDefinitionAndSpecifications.buildNewBbgCurveDefinitions();
      for (final Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : newCurveDefinitions.entrySet()) {
        final String curveName = entry.getKey();
        final Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
        for (final Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
          final Currency ccy = currencyEntry.getKey();
          final YieldCurveDefinition definition = currencyEntry.getValue();
          final ConfigItem<YieldCurveDefinition> item = ConfigItem.of(definition);
          item.setName(curveName + "_" + ccy.getCode());
          ConfigMasterUtils.storeByName(configMaster, item);
          dumpDefinition(definition);
        }
      }
    }
  }

  public static void populateCurveSpecificationBuilderConfigMaster(final ConfigMaster configMaster) {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = CurveDefinitionAndSpecifications.buildStandardCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : configurations.entrySet()) {
      final ConfigItem<CurveSpecificationBuilderConfiguration> item = ConfigItem.of(entry.getValue());
      item.setName("DEFAULT_" + entry.getKey().getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
    }
  }

  public static void populateSyntheticCurveSpecificationBuilderConfigMaster(final ConfigMaster configMaster) {
    final Map<Currency, CurveSpecificationBuilderConfiguration> syntheticConfigurations = CurveDefinitionAndSpecifications.buildSyntheticCurveSpecificationBuilderConfigurations();
    for (final Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : syntheticConfigurations.entrySet()) {
      final ConfigItem<CurveSpecificationBuilderConfiguration> item = ConfigItem.of(entry.getValue());
      item.setName("SECONDARY_" + entry.getKey().getCode());
      ConfigMasterUtils.storeByName(configMaster, item);
    }
    final Currency ccy = Currency.AUD;
    final CurveSpecificationBuilderConfiguration audCurveSpec3M = CurveDefinitionAndSpecifications.buildSyntheticAUD3MCurveSpecification();
    final CurveSpecificationBuilderConfiguration audCurveSpec6M = CurveDefinitionAndSpecifications.buildSyntheticAUD6MCurveSpecification();
    ConfigItem<CurveSpecificationBuilderConfiguration> item = ConfigItem.of(audCurveSpec3M);
    item.setName("SECONDARY_3M_" + ccy.getCode());
    ConfigMasterUtils.storeByName(configMaster, item);
    item = ConfigItem.of(audCurveSpec6M);
    item.setName("SECONDARY_6M_" + ccy.getCode());
    ConfigMasterUtils.storeByName(configMaster, item);
  }

}
