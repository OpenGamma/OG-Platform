/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing frequency.
 */
public class FrequencyBean extends EnumBean {

  protected FrequencyBean() {
  }

  public FrequencyBean(String frequency) {
    super(frequency);
  }

}
