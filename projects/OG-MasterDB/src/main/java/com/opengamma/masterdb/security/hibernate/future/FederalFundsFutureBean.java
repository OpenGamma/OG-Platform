/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.financial.security.future.FederalFundsFutureSecurity;

/**
 * A Hibernate bean representation of {@link FederalFundsFutureSecurity}.
 */
public class FederalFundsFutureBean extends FutureSecurityBean {

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitFederalFundsFutureType(this);
  }

}
