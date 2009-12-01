/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import javax.persistence.Entity;

/**
 * 
 *
 * @author jim
 */
@Entity
public class ExchangeBean extends EnumWithDescriptionBean {
  protected ExchangeBean() {
  }

  public ExchangeBean(String exchangeName, String description) {
    super(exchangeName, description);
  }
}
