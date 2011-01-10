/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
