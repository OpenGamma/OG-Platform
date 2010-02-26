/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import javax.persistence.Entity;

@Entity
public class BusinessDayConventionBean extends EnumBean {
  protected BusinessDayConventionBean() {
  }

  public BusinessDayConventionBean(String conventionName) {
    super(conventionName);
  }
}
