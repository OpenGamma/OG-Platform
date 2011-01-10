/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.core.common.Currency;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * This class should be removed, it is here just to ease integration with existing tests.
 * NOTE: MongoDBMasterConfigSource has been hacked to give access to getConfigMasterFor. This should be
 * reversed. 
 */
public class DefaultInterpolatedYieldAndDiscountCurveSource extends ConfigDBInterpolatedYieldCurveDefinitionSource {
  // TODO: Remove this class which exists to make old tests easier

  /**
   * Creates an instance.
   * @param source  the source, not null
   */
  public DefaultInterpolatedYieldAndDiscountCurveSource(final MasterConfigSource source) {
    super(source);
  }

  /**
   * Adds a definition to the configuration.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param definition  the definition, not null
   */
  public void addDefinition(Currency currency, String name, YieldCurveDefinition definition) {
    MasterConfigSource configSource = (MasterConfigSource) getConfigSource();
    ConfigTypeMaster<YieldCurveDefinition> configMaster = ((MasterConfigSource) configSource).getMaster().typed(YieldCurveDefinition.class);
    ConfigDocument<YieldCurveDefinition> doc = new ConfigDocument<YieldCurveDefinition>();
    doc.setName(name + "_" + currency.getISOCode());
    doc.setValue(definition);
    ConfigMasterUtils.storeByName(configMaster, doc);
  }

}
