/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;


/**
 * 
 */
public class WebViewGridColumn implements Comparable<WebViewGridColumn> {
  
  private final int _id;
  private final String _header;
  private final String _description;
  private final String _valueName;
  private boolean _typeKnown;
  
  public WebViewGridColumn(int id, String header, String description, String valueName) {
    _id = id;
    _header = header;
    _description = description;
    _valueName = valueName;
  }

  public int getId() {
    return _id;
  }
  
  public String getHeader() {
    return _header;
  }
  
  public String getDescription() {
    return _description;
  }

  public String getValueName() {
    return _valueName;
  }
  
  public boolean isTypeKnown() {
    return _typeKnown;
  }
  
  public void setTypeKnown(boolean typeKnown) {
    _typeKnown = typeKnown;
  }

  @Override
  public int compareTo(WebViewGridColumn o) {
    return 0;
  }

}
