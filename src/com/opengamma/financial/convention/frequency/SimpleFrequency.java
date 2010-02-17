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
  
  public static final Frequency MONTHLY = new SimpleFrequency (12, "monthly");
  public static final Frequency ANNUALLY = new SimpleFrequency (1, "annually");
  public static final Frequency SEMI_ANNUALLY = new SimpleFrequency (6, "bi-annually");
  public static final Frequency QUARTERLY = new SimpleFrequency (4, "quarterly");
  
  private final int _frequency;
  private final String _name;
  
  protected SimpleFrequency (final int frequency, final String name) {
    _frequency = frequency;
    _name = name;
  }
  
  public int getFrequency () {
    return _frequency;
  }
  
  public String getConventionName () {
    return _name;
  }
  
}