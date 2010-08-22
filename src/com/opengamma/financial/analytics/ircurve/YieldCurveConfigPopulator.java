/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.config.ConfigMaster;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.financial.analytics.ircurve.CurveDefinitionAndSpecifications;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;

/**
 * 
 */
public class YieldCurveConfigPopulator {
  
  public static void populateCurveDefinitionConfigRepository(MongoDBMasterConfigSource masterConfigSource) {
    ConfigMaster<YieldCurveDefinition> curveDefinitionMaster = masterConfigSource.getConfigMasterFor(YieldCurveDefinition.class);
    populateCurveDefinitionConfigRepository(curveDefinitionMaster);
    ConfigMaster<CurveSpecificationBuilderConfiguration> curveSpecificationBuilderConfigMaster = masterConfigSource.getConfigMasterFor(CurveSpecificationBuilderConfiguration.class);
    populateCurveSpecificationBuilderConfigRepository(curveSpecificationBuilderConfigMaster);
  }
  
  public static void populateCurveDefinitionConfigRepository(ConfigMaster<YieldCurveDefinition> configRepo) {
    DefaultConfigDocument<YieldCurveDefinition> forwardUSD = new DefaultConfigDocument<YieldCurveDefinition>();
    forwardUSD.setName("FORWARD_USD");
    forwardUSD.setValue(CurveDefinitionAndSpecifications.buildUSDForwardCurveDefinition());
    configRepo.add(forwardUSD);
    DefaultConfigDocument<YieldCurveDefinition> fundingUSD = new DefaultConfigDocument<YieldCurveDefinition>();
    fundingUSD.setName("FUNDING_USD");
    fundingUSD.setValue(CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition());
    configRepo.add(fundingUSD);
  }
  
  public static void populateCurveSpecificationBuilderConfigRepository(ConfigMaster<CurveSpecificationBuilderConfiguration> configMaster) {
    DefaultConfigDocument<CurveSpecificationBuilderConfiguration> doc = new DefaultConfigDocument<CurveSpecificationBuilderConfiguration>();
    doc.setName("DEFAULT_USD");
    doc.setValue(CurveDefinitionAndSpecifications.buildUSDCurveSpecificationBuilderConfiguration());
    configMaster.add(doc);
  }


}
