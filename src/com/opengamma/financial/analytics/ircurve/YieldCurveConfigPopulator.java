/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.config.ConfigMaster;
import com.opengamma.config.DefaultConfigDocument;
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
    DefaultConfigDocument<YieldCurveDefinition> forwardUSD = new DefaultConfigDocument<YieldCurveDefinition>();
    forwardUSD.setName("FORWARD_USD");
    forwardUSD.setValue(CurveDefinitionAndSpecifications.buildUSDForwardCurveDefinition());
    configRepo.add(forwardUSD);
    DefaultConfigDocument<YieldCurveDefinition> fundingUSD = new DefaultConfigDocument<YieldCurveDefinition>();
    fundingUSD.setName("FUNDING_USD");
    fundingUSD.setValue(CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition());
    configRepo.add(fundingUSD);
    DefaultConfigDocument<YieldCurveDefinition> swapOnlyUSD = new DefaultConfigDocument<YieldCurveDefinition>();
    swapOnlyUSD.setName("SWAP_ONLY_USD");
    swapOnlyUSD.setValue(CurveDefinitionAndSpecifications.buildUSDSwapOnlyCurveDefinition());
    configRepo.add(swapOnlyUSD);
  }
  
  public static void populateCurveSpecificationBuilderConfigMaster(ConfigMaster<CurveSpecificationBuilderConfiguration> configMaster) {
    DefaultConfigDocument<CurveSpecificationBuilderConfiguration> doc = new DefaultConfigDocument<CurveSpecificationBuilderConfiguration>();
    doc.setName("DEFAULT_USD");
    doc.setValue(CurveDefinitionAndSpecifications.buildUSDCurveSpecificationBuilderConfiguration());
    configMaster.add(doc);
  }
}
