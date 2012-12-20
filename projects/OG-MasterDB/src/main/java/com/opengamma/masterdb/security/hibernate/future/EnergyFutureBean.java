/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.financial.security.future.EnergyFutureSecurity;

/**
 * A Hibernate bean representation of {@link EnergyFutureSecurity}.
 */
public class EnergyFutureBean extends CommodityFutureBean {

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitEnergyFutureType(this);
  }

}
