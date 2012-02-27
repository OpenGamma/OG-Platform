/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.batch.domain.*;
import com.opengamma.util.db.HibernateMappingFiles;

/**
 * DbBatchWriter configuration.
 */
public class HibernateBatchDbFiles implements HibernateMappingFiles {

  @Override
  public Class<?>[] getHibernateMappingFiles() {
    return new Class[] {
      CalculationConfiguration.class,
      ComputeHost.class,
      ComputeNode.class,
      LiveDataField.class,
      MarketData.class,
      MarketDataValue.class,
      RiskRun.class,
      RiskValueRequirement.class,
      RiskValueSpecification.class,
      FunctionUniqueId.class,
      HbComputationTargetSpecification.class,
      RiskRunProperty.class
    };
  }

}
