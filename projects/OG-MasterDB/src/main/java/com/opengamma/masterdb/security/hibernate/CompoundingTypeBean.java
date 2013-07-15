/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate bean for storing the compounding type for swap legs.
 */
public class CompoundingTypeBean extends EnumBean {

  protected CompoundingTypeBean() {
  }

  /**
   * @param compoundingTypeName The compoundingType
   */
  public CompoundingTypeBean(String compoundingTypeName) {
    super(compoundingTypeName);
  }

}
