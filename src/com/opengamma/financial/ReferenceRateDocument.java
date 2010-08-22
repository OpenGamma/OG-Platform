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
  private ReferenceRate _referenceRate;
  
  public ReferenceRateDocument(ReferenceRate referenceRate) {
    _referenceRate = referenceRate;
    _name = referenceRate.getName();
  }
  
  public String getName() {
    return _name;
  }
  
  public ReferenceRate getReferenceRate() {
    return _referenceRate;
  }
  
  public ReferenceRate getValue() {
    return getReferenceRate();
  }
}
