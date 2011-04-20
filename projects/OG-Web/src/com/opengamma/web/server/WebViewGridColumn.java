/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;


/**
 * 
 */
public class WebViewGridColumn {
  
  private final long _id;
  private final String _key;
  private final String _valueRequirementName;
  private boolean _typeKnown;
  
  public WebViewGridColumn(long id, String key, String valueRequirementName) {
    _id = id;
    _key = key;
    _valueRequirementName = valueRequirementName;
  }

  public long getId() {
    return _id;
  }
  
  public String getKey() {
    return _key;
  }

  public String getValueRequirementName() {
    return _valueRequirementName;
  }
  
  public boolean isTypeKnown() {
    return _typeKnown;
  }
  
  public void setTypeKnown(boolean typeKnown) {
    _typeKnown = typeKnown;
  }

}
