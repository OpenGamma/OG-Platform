/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing schedule stub type
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class StubTypeBean extends EnumBean {

  protected StubTypeBean() {
  }

  public StubTypeBean(String stubTypeName) {
    super(stubTypeName);
  }
}
