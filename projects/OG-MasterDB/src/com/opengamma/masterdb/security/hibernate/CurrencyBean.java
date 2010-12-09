/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing a currency.
 */
public class CurrencyBean extends EnumBean {

  protected CurrencyBean() {
  }

  public CurrencyBean(String isoCode) {
    super(isoCode);
  }

}
