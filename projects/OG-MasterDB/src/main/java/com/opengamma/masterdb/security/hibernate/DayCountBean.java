/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

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
