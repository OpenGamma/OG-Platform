/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;


/**
 * Document to hold the reference rate and meta-data
 */
public class ConventionBundleDocument {
  private String _name;
  private ConventionBundle _conventionSet;
  
  public ConventionBundleDocument(ConventionBundle conventionSet) {
    _conventionSet = conventionSet;
    _name = conventionSet.getName();
  }
  
  public String getName() {
    return _name;
  }
  
  public ConventionBundle getConventionSet() {
    return _conventionSet;
  }
  
  public ConventionBundle getValue() {
    return getConventionSet();
  }
}
