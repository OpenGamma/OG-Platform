/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.financial.security.master.db.hibernate.EnumBean;

/**
 * Hibernate bean for storing an issuer type.
 */
public class IssuerTypeBean extends EnumBean {

  protected IssuerTypeBean() {
  }

  public IssuerTypeBean(String issuerType) {
    super(issuerType);
  }

}
