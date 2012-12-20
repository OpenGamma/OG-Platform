/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.List;

import com.google.common.collect.ImmutableList;


/**
 * Empty black list of values
 */
public final class EmptyBlackList implements BlackList {
  
  /**
   * Singleton instance
   */
  public static final EmptyBlackList INSTANCE = new EmptyBlackList();
  
  private String _name;
  
  private List<String> _blackList; 
  
  private EmptyBlackList() {
    _name = "EMPTY";
    _blackList = ImmutableList.of();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public List<String> getBlackList() {
    return _blackList;
  }

}
