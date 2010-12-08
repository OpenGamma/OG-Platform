/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate;

/**
 * Hibernate bean for storing a day count convention.
 */
public class DayCountBean extends EnumBean {

  protected DayCountBean() {
  }

  public DayCountBean(String conventionName) {
    super(conventionName);
  }

}
