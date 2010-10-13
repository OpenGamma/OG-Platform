/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.engine.config.MongoDBMasterConfigSource;

/**
 * 
 */
public class YieldCurveConfigPopulator {
  
  public static MongoDBMasterConfigSource populateCurveConfigSource(MongoDBMasterConfigSource masterConfigSource) {
    ConfigMaster<YieldCurveDefinition> curveDefinitionMaster = masterConfigSource.getConfigMasterFor(YieldCurveDefinition.class);
    populateCurveDefinitionConfigMaster(curveDefinitionMaster);
    ConfigMaster<CurveSpecificationBuilderConfiguration> curveSpecificationBuilderConfigMaster = masterConfigSource.getConfigMasterFor(CurveSpecificationBuilderConfiguration.class);
    populateCurveSpecificationBuilderConfigMaster(curveSpecificationBuilderConfigMaster);
    return masterConfigSource;
  }
  
  public static void populateCurveDefinitionConfigMaster(ConfigMaster<YieldCurveDefinition> configRepo) {
    ConfigDocument<YieldCurveDefinition> forwardUSD = new ConfigDocument<YieldCurveDefinition>();
    forwardUSD.setName("FORWARD_USD");
    forwardUSD.setValue(CurveDefinitionAndSpecifications.buildUSDForwardCurveDefinition());
    configRepo.add(forwardUSD);
    ConfigDocument<YieldCurveDefinition> fundingUSD = new ConfigDocument<YieldCurveDefinition>();
    fundingUSD.setName("FUNDING_USD");
    fundingUSD.setValue(CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition());
    configRepo.add(fundingUSD);
    ConfigDocument<YieldCurveDefinition> swapOnlyUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnlyUSD.setName("SWAP_ONLY_USD");
    swapOnlyUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyCurveDefinition());
    configRepo.add(swapOnlyUSD);
    ConfigDocument<YieldCurveDefinition> swapOnlyNo3YrUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnlyNo3YrUSD.setName("SWAP_ONLY_NO3YR_USD");
    swapOnlyNo3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyNo3YrCurveDefinition());
    configRepo.add(swapOnlyNo3YrUSD);
    ConfigDocument<YieldCurveDefinition> swapOnly3YrUSD = new ConfigDocument<YieldCurveDefinition>();
    swapOnly3YrUSD.setName("SWAP_ONLY_3YR_USD");
    swapOnly3YrUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnly3YrCurveDefinition());
    configRepo.add(swapOnly3YrUSD);
    ConfigDocument<YieldCurveDefinition> singleUSD = new ConfigDocument<YieldCurveDefinition>();
    singleUSD.setName("SINGLE_USD");
    singleUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSingleCurveDefinition());
    configRepo.add(singleUSD);
  }
  
  public static void populateCurveSpecificationBuilderConfigMaster(ConfigMaster<CurveSpecificationBuilderConfiguration> configMaster) {
    ConfigDocument<CurveSpecificationBuilderConfiguration> doc = new ConfigDocument<CurveSpecificationBuilderConfiguration>();
    doc.setName("DEFAULT_USD");
    doc.setValue(CurveDefinitionAndSpecifications.buildUSDCurveSpecificationBuilderConfiguration());
    configMaster.add(doc);
  }

}
