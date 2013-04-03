/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing a credit default swap index family.
 */
public class CDSIndexFamilyBean extends EnumBean {

  protected CDSIndexFamilyBean() {
  }

  public CDSIndexFamilyBean(final String family) {
    super(family);
  }

}
