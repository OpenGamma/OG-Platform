/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.Currency;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigTypeMaster;

/**
 * Populates the yield curve configuration.
 */
public class YieldCurveConfigPopulator {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveConfigPopulator.class);

  public YieldCurveConfigPopulator(ConfigMaster cfgMaster) {
    populateCurveConfigMaster(cfgMaster);
  }
  
  public static ConfigMaster populateCurveConfigMaster(ConfigMaster cfgMaster) {
    ConfigTypeMaster<YieldCurveDefinition> curveDefinitionMaster = cfgMaster.typed(YieldCurveDefinition.class);
    populateCurveDefinitionConfigMaster(curveDefinitionMaster);
    ConfigTypeMaster<CurveSpecificationBuilderConfiguration> curveSpecificationBuilderConfigMaster = cfgMaster.typed(CurveSpecificationBuilderConfiguration.class);
    populateCurveSpecificationBuilderConfigMaster(curveSpecificationBuilderConfigMaster);
    return cfgMaster;
  }
  
  private static void dumpDefinition(YieldCurveDefinition curveDefinition) {
    s_logger.debug("Curve Definition");
    s_logger.debug("  Name:" + curveDefinition.getName());
    s_logger.debug("  Currency:" + curveDefinition.getCurrency());
    s_logger.debug("  Strips:");
    for (FixedIncomeStrip strip : curveDefinition.getStrips()) {
      s_logger.debug("    " + strip);
    }
  }

  public static void populateCurveDefinitionConfigMaster(ConfigTypeMaster<YieldCurveDefinition> configMaster) {
    Map<String, Map<Currency, YieldCurveDefinition>> standardCurveDefinitions = CurveDefinitionAndSpecifications.buildStandardCurveDefintions();
    for (Map.Entry<String, Map<Currency, YieldCurveDefinition>> entry : standardCurveDefinitions.entrySet()) {
      String curveName = entry.getKey();
      Map<Currency, YieldCurveDefinition> definitions = entry.getValue();
      for (Map.Entry<Currency, YieldCurveDefinition> currencyEntry : definitions.entrySet()) {
        Currency ccy = currencyEntry.getKey();
        YieldCurveDefinition definition = currencyEntry.getValue();
        ConfigDocument<YieldCurveDefinition> document = new ConfigDocument<YieldCurveDefinition>();
        document.setName(curveName + "_" + ccy.getISOCode());
        document.setValue(definition);
        ConfigMasterUtils.storeByName(configMaster, document);
        dumpDefinition(definition);
      }
    }
//    ConfigDocument<YieldCurveDefinition> forwardUSD = new ConfigDocument<YieldCurveDefinition>();
//    forwardUSD.setName("FORWARD_USD");
//    forwardUSD.setValue(CurveDefinitionAndSpecifications.buildUSDForwardCurveDefinition());
//    ConfigMasterUtils.storeByName(configRepo, forwardUSD);
//    ConfigDocument<YieldCurveDefinition> fundingUSD = new ConfigDocument<YieldCurveDefinition>();
//    fundingUSD.setName("FUNDING_USD");
//    fundingUSD.setValue(CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition());
//    ConfigMasterUtils.storeByName(configRepo, fundingUSD);
    ConfigDocument<YieldCurveDefinition> swapOnlyUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnlyUSD.setName("SWAP_ONLY_USD");
    swapOnlyUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnlyUSD);
    ConfigDocument<YieldCurveDefinition> swapOnlyNo3YrUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnlyNo3YrUSD.setName("SWAP_ONLY_NO3YR_USD");
    swapOnlyNo3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyNo3YrCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnlyNo3YrUSD);
    ConfigDocument<YieldCurveDefinition> swapOnly3YrUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnly3YrUSD.setName("SWAP_ONLY_3YR_USD");
    swapOnly3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnly3YrCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, swapOnly3YrUSD);
    ConfigDocument<YieldCurveDefinition> singleUSD = new ConfigDocument<YieldCurveDefinition>();
    singleUSD.setName("SINGLE_USD");
    singleUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSingleCurveDefinition());
    ConfigMasterUtils.storeByName(configMaster, singleUSD);
  }

  public static void populateCurveSpecificationBuilderConfigMaster(ConfigTypeMaster<CurveSpecificationBuilderConfiguration> configMaster) {
    Map<Currency, CurveSpecificationBuilderConfiguration> configurations = CurveDefinitionAndSpecifications.buildStandardCurveSpecificationBuilderConfigurations();
    for (Map.Entry<Currency, CurveSpecificationBuilderConfiguration> entry : configurations.entrySet()) {
      ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>();
      doc.setName("DEFAULT_" + entry.getKey().getISOCode());
      doc.setValue(entry.getValue());
      ConfigMasterUtils.storeByName(configMaster, doc);
    }
//    ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>();
//    doc.setName("DEFAULT_USD");
//    doc.setValue(CurveDefinitionAndSpecifications.buildUSDCurveSpecificationBuilderConfiguration());
    
  }

}
