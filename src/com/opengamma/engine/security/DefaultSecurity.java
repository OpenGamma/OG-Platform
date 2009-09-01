/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private Collection<SecurityIdentifier> _identifiers;

  @Override
  public Collection<SecurityIdentifier> getIdentifiers() {
    return _identifiers;
  }

  /**
   * This will create a <em>copy</em> of the provided collection.
   * @param identifiers the identifiers to set
   */
  public void setIdentifiers(Collection<? extends SecurityIdentifier> identifiers) {
    _identifiers = new ArrayList<SecurityIdentifier>(identifiers);
  }

}
