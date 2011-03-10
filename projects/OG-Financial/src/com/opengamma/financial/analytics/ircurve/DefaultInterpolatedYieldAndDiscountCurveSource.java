/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
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
  public void addDefinition(CurrencyUnit currency, String name, YieldCurveDefinition definition) {
    MasterConfigSource configSource = (MasterConfigSource) getConfigSource();
    ConfigDocument<YieldCurveDefinition> doc = new ConfigDocument<YieldCurveDefinition>();
    doc.setName(name + "_" + currency.getCode());
    doc.setValue(definition);
    ConfigMasterUtils.storeByName(configSource.getMaster(), doc);
  }

}
