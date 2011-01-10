/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.time.Expiry;

/**
 *
 */
public class ForwardDefinition {
  private final Expiry _expiry;

  public ForwardDefinition(final Expiry expiry) {
    Validate.notNull(expiry, "expiry");
    _expiry = expiry;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expiry == null) ? 0 : _expiry.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForwardDefinition other = (ForwardDefinition) obj;
    return ObjectUtils.equals(_expiry, other._expiry);
  }
}
