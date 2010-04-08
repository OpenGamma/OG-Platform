/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

public class DayCountBean extends EnumBean {
  protected DayCountBean() {
  }

  public DayCountBean(String conventionName) {
    super(conventionName);
  }
}
