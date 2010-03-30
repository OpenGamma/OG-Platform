/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 *
 */
public class ForwardDefinition {
  private final Expiry _expiry;

  public ForwardDefinition(final Expiry expiry) {
    if (expiry == null)
      throw new IllegalArgumentException("Expiry was null");
    _expiry = expiry;
  }

  public Expiry getExpiry() {
    return _expiry;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expiry == null) ? 0 : _expiry.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ForwardDefinition other = (ForwardDefinition) obj;
    if (_expiry == null) {
      if (other._expiry != null)
        return false;
    } else if (!_expiry.equals(other._expiry))
      return false;
    return true;
  }
}
