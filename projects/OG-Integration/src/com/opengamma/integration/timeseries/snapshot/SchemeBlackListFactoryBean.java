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
 * Factory bean to provide list of blackListed schemes.
 * <p>
 * This class provides a simple-to-setup and simple-to-use way to create a blackListed schemes
 * The main benefit is simpler configuration, especially if that configuration is in XML.
 */
public class SchemeBlackListFactoryBean extends SingletonFactoryBean<SchemeBlackList> {
  
  private String _name;
  
  private List<String> _schemeBlackList;
  
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
   * Gets the schemeBlackList.
   * @return the schemeBlackList
   */
  public List<String> getSchemeBlackList() {
    return _schemeBlackList;
  }

  /**
   * Sets the schemeBlackList.
   * @param schemeBlackList  the schemeBlackList
   */
  public void setSchemeBlackList(List<String> schemeBlackList) {
    _schemeBlackList = schemeBlackList;
  }

  @Override
  public SchemeBlackList createObject() {
    final String name = getName();  // store in variable to protect against change by subclass
    ArgumentChecker.notNull(name, "name");
    
    DefaultSchemeBlackList blackList = new DefaultSchemeBlackList();
    blackList.setName(name);
    blackList.setSchemeBlackList(getSchemeBlackList());
    return blackList;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
