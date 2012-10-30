/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.forward;

import com.opengamma.financial.security.forward.EnergyForwardSecurity;

/**
 * A Hibernate bean representation of {@link EnergyForwardSecurity}.
 */
public class EnergyForwardSecurityBean extends CommodityForwardSecurityBean {

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitEnergyForwardType(this);
  }

}
