/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;


/**
 * Document to hold the reference rate and meta-data
 */
public class ConventionDocument {
  private final String _name;
  private final Convention _conventionSet;

  public ConventionDocument(final Convention conventionSet) {
    _conventionSet = conventionSet;
    _name = conventionSet.getName();
  }

  public String getName() {
    return _name;
  }

  public Convention getConventionSet() {
    return _conventionSet;
  }

  public Convention getValue() {
    return getConventionSet();
  }
}
