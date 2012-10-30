/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.List;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean to provide a list of blackListed data fields.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to create a blackListed data fields
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 */
public class DataFieldBlackListFactoryBean extends SingletonFactoryBean<DataFieldBlackList> {
  
  private String _name;
  
  private List<String> _dataFieldBlackList;
  
  /**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Gets the dataFieldBlackList.
   * @return the dataFieldBlackList
   */
  public List<String> getDataFieldBlackList() {
    return _dataFieldBlackList;
  }

  /**
   * Sets the dataFieldBlackList.
   * @param dataFieldBlackList  the dataFieldBlackList
   */
  public void setDataFieldBlackList(List<String> dataFieldBlackList) {
    _dataFieldBlackList = dataFieldBlackList;
  }

  @Override
  public DataFieldBlackList createObject() {
    final String name = getName();  // store in variable to protect against change by subclass
    ArgumentChecker.notNull(name, "name");
    
    DefaultDataFieldBlackList blackList = new DefaultDataFieldBlackList();
    blackList.setName(name);
    blackList.setDataFieldBlackList(getDataFieldBlackList());
    return blackList;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
