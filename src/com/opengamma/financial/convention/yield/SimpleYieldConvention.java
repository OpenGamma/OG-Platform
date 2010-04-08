/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;


/**
 * 
 *
 * @author Andrew
 */
public class SimpleYieldConvention implements YieldConvention {
  
  public static final YieldConvention US_STREET = new SimpleYieldConvention ("US street");
  public static final YieldConvention US_TREASURY_EQUIVILANT = new SimpleYieldConvention ("US Treasury equivilant");
  public static final YieldConvention MONEY_MARKET = new SimpleYieldConvention ("Money Market");
  public static final YieldConvention JGB_SIMPLE = new SimpleYieldConvention ("JGB simple");
  public static final YieldConvention TRUE = new SimpleYieldConvention ("True");
  
  private final String _name;
  
  protected SimpleYieldConvention (final String name) {
    _name = name;
  }
  
  @Override
  public String getConventionName () {
    return _name;
  }
  
}