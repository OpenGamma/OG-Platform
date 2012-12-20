/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing a business day convention.
 */
public class BusinessDayConventionBean extends EnumBean {

  protected BusinessDayConventionBean() {
  }

  public BusinessDayConventionBean(String conventionName) {
    super(conventionName);
  }

}
