/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate storage for an exchange.
 */
public class ExchangeBean extends EnumWithDescriptionBean {

  protected ExchangeBean() {
  }

  public ExchangeBean(String exchangeName, String description) {
    super(exchangeName, description);
  }

}
