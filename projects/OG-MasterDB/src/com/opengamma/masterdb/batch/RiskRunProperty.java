/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;


/**
 * 
 */
public class RiskRunProperty {
  
  private int _id;
  private RiskRun _riskRun;
  private String _propertyKey;
  private String _propertyValue;
  
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
  
  public String getPropertyKey() {
    return _propertyKey;
  }
  
  public void setPropertyKey(String propertyKey) {
    _propertyKey = propertyKey;
  }
  
  public String getPropertyValue() {
    return _propertyValue;
  }
  
  public void setPropertyValue(String propertyValue) {
    _propertyValue = propertyValue;
  }
  
}
