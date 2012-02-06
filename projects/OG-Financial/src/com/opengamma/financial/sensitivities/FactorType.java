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
}
