/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.financial.Currency;

/**
 * This class should be removed, it is here just to ease integration with existing tests.
 * NOTE: MongoDBMasterConfigSource has been hacked to give access to getConfigMasterFor. This should be
 * reversed. 
 */
public class DefaultInterpolatedYieldAndDiscountCurveSource extends ConfigDBInterpolatedYieldCurveDefinitionSource {

  public DefaultInterpolatedYieldAndDiscountCurveSource() {
    super(new MongoDBMasterConfigSource());
  }
  
  public void addDefinition(Currency currency, String name, YieldCurveDefinition definition) {
    MongoDBMasterConfigSource configSource = (MongoDBMasterConfigSource) getConfigSource();
    MongoDBConfigMaster<YieldCurveDefinition> configMaster = configSource.getConfigMasterFor(YieldCurveDefinition.class);
    ConfigDocument<YieldCurveDefinition> doc = new ConfigDocument<YieldCurveDefinition>();
    doc.setName(name + "_" + currency.getISOCode());
    doc.setValue(definition);
    configMaster.add(doc);
  }
}
