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
public interface Frequency {
  
  // TODO what methods would be needed here to support meaningful calculations of coupon payouts from e.g. a bond?
  
  public int getFrequency ();
  
  public String getConventionName ();
  
}