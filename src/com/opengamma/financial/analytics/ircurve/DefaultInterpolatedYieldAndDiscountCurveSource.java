/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.config.db.MongoDBConfigRepository;
import com.opengamma.financial.Currency;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * This class should be removed, it is here just to ease integration with existing tests. 
 */
public class DefaultInterpolatedYieldAndDiscountCurveSource extends ConfigDBInterpolatedYieldCurveDefinitionSource {

  public DefaultInterpolatedYieldAndDiscountCurveSource() {
    super(new MongoDBConfigRepository<YieldCurveDefinition>(YieldCurveDefinition.class, new MongoDBConnectionSettings()));
  }
  
  public void addDefinition(Currency currency, String name, YieldCurveDefinition definition) {
    getRepo().insertNewItem(name + "_" + currency.getISOCode(), definition);
  }
}
