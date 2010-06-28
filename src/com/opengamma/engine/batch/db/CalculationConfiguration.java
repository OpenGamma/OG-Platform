/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class CalculationConfiguration {
  
  private int _id;
  private RiskRun _riskRun;
  private String _name;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public RiskRun getRiskRun() {
    return _riskRun;
  }

  public void setRiskRun(RiskRun riskRun) {
    _riskRun = riskRun;
  }

  public String getName() {
    return _name;
  }
  
  public void setName(String name) {
    _name = name;
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return new ToStringBuilder(this).
      append("name", getName()).
      toString();
  }
  

}
