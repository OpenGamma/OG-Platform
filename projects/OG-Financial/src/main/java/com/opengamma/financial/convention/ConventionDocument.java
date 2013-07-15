/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;


/**
 * Document to hold a convention and its associated meta-data.
 */
public class ConventionDocument {
  /** The name of the convention */
  private final String _name;
  /** The convention */
  private final Convention _convention;

  /**
   * @param convention The convention, not null
   */
  public ConventionDocument(final Convention convention) {
    ArgumentChecker.notNull(convention, "convention");
    _convention = convention;
    _name = convention.getName();
  }

  /**
   * Gets the name of the convention.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the convention.
   * @return The convention
   */
  public Convention getConvention() {
    return _convention;
  }

  /**
   * Gets the convention.
   * @return The convention
   */
  public Convention getValue() {
    return getConvention();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionDocument)) {
      return false;
    }
    final ConventionDocument other = (ConventionDocument) obj;
    return ObjectUtils.equals(_convention, other._convention);
  }

}
