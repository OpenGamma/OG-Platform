/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;


/**
 * Document to hold a convention and its associated meta-data.
 */
public class ConventionDocument extends AbstractDocument {
  /** The name of the convention */
  private String _name;
  /** The convention */
  private Convention _convention;
  /** The UniqueId */
  private UniqueId _uniqueId;

  /**
   * @param convention The convention, not null
   */
  public ConventionDocument(final Convention convention) {
    ArgumentChecker.notNull(convention, "convention");
    _convention = convention;
    _name = convention.getName();
    _uniqueId = convention.getUniqueId();
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
  * Gets the convention id.
  * @return The convention
  */
  public UniqueIdentifiable getValue() {
    return _uniqueId;
  }

  /**
   * Gets the UniqueId.
   * @return The convention
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
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

  public void setUniqueId(final UniqueId id) {
    _uniqueId = id;
  }

}
