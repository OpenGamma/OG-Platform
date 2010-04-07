/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

/**
 * 
 *
 * @author Andrew
 */
public class SimpleFrequency implements Frequency {
  
  public static final Frequency MONTHLY = new SimpleFrequency ("monthly");
  public static final Frequency ANNUALLY = new SimpleFrequency ("annually");
  public static final Frequency SEMI_ANNUALLY = new SimpleFrequency ("bi-annually");
  public static final Frequency QUARTERLY = new SimpleFrequency ("quarterly");
  
  private final String _name;
  
  protected SimpleFrequency (final String name) {
    _name = name;
  }
  
  @Override
  public String getConventionName () {
    return _name;
  }
  
}