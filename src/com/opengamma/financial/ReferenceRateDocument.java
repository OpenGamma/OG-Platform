/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

/**
 * Document to hold the reference rate and meta-data
 */
public class ReferenceRateDocument {
  private String _name;
  
  public ReferenceRateDocument(ReferenceRate referenceRate) {
    _name = referenceRate.getName();
  }
  
  public String getName() {
    return _name;
  }
}
