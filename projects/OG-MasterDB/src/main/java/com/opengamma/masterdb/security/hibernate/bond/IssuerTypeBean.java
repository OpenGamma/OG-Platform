/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.masterdb.security.hibernate.EnumBean;

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
