/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.financial.security.future.AgricultureFutureSecurity;

/**
 * A Hibernate bean representation of {@link AgricultureFutureSecurity}.
 */
public class AgricultureFutureBean extends CommodityFutureBean {

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitAgricultureFutureType(this);
  }

}
