/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

/**
 * Holds the factor type of a FactorExposureEntry.
 */
public final class FactorType {
  /**
   * A constant representing yield curve risk factors
   */
  public static final FactorType YIELD = new FactorType("yieldRiskFactor");
  /**
   * A constant representing volatility risk factors
   */
  public static final FactorType VOLATILITY = new FactorType("volatilityRiskFactor");
  /**
   * A constant representing CDS spread risk factors
   */
  public static final FactorType CDS_SPREAD = new FactorType("CDSSpreadRiskFactor");
  /**
   * A constant representing equity risk factors
   */
  public static final FactorType EQUITY = new FactorType("equityRiskFactor");
  
  private String _type;

  private FactorType(String type) {
    _type = type;
  }
  
  public static FactorType of(String factorType) {
    return new FactorType(factorType);
  }
  
  public String getFactorType() {
    return _type;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof FactorType)) {
      return false;
    }
    FactorType other = (FactorType) o;
    return other.getFactorType().equals(getFactorType());
  }
  
  public int hashCode() {
    return getFactorType().hashCode();
  }
  
  public String toString() {
    return getFactorType();
  }
}
