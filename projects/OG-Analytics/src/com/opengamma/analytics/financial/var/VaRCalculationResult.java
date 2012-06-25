/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;


/**
 * 
 */
public class VaRCalculationResult {
  // REVIEW kirk 2012-06-22 -- Can these be double rather than Double?
  // I'm following the existing contract.
  private final Double _varValue;
  private final Double _stdDev;
  
  public VaRCalculationResult(Double varValue, Double stdDev) {
    _varValue = varValue;
    _stdDev = stdDev;
  }
  
  public Double getVaRValue() {
    return _varValue;
  }
  
  public Double getStdDev() {
    return _stdDev;
  }

}
