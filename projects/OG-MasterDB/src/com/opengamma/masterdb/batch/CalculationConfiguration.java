/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class CalculationConfiguration {
  
  private int _id = -1;
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
  public String toString() {
    return new ToStringBuilder(this).
      append("name", getName()).
      toString();
  }
  

}
