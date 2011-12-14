/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Hibernate bean.
 */
public class CalculationConfiguration {
  
  private int _id = -1;
  private String _name;

  public CalculationConfiguration(String name) {
    _name = name;
  }

  public CalculationConfiguration() {
  }

  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }

  public String getName() {
    return _name;
  }
  
  public void setName(String name) {
    _name = name;
  }
  
  @Override
  public String toString() {
    return new ToStringBuilder(this).
      append("name", getName()).
      toString();
  }

}
