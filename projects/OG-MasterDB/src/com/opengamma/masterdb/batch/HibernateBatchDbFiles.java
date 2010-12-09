/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.util.db.HibernateMappingFiles;

/**
 * BatchDbManagerImpl configuration. 
 */
public class HibernateBatchDbFiles implements HibernateMappingFiles {

  @Override
  public Class<?>[] getHibernateMappingFiles() {
    return new Class[] {
      CalculationConfiguration.class,
      ComputeHost.class,
      ComputeNode.class,
      LiveDataField.class,
      LiveDataSnapshot.class,
      LiveDataSnapshotEntry.class,
      ObservationDateTime.class,
      ObservationTime.class,
      OpenGammaVersion.class,
      RiskRun.class,
      RiskValueName.class,
      FunctionUniqueId.class,
      ComputationTarget.class,
      RiskValue.class,
      RiskRunProperty.class
    };
  }
}
