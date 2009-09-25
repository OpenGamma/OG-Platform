/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// REVIEW kirk 2009-09-01 -- I'm not particularly happy with the name of this class.

/**
 * A concrete, immutable implementation of {@link SecurityKey}. 
 *
 * @author kirk
 */
public class SecurityKeyImpl implements SecurityKey, Serializable {
  
  private final List<SecurityIdentifier> _identifiers;
  
  public SecurityKeyImpl(SecurityIdentifier... identifiers) {
    if((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<SecurityIdentifier>(identifiers.length);
      for(SecurityIdentifier secId : identifiers) {
        _identifiers.add(secId);
      }
    }
  }
  
  public SecurityKeyImpl(Collection<? extends SecurityIdentifier> identifiers) {
    if(identifiers == null) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<SecurityIdentifier>(identifiers);
    }
  }
  
  public SecurityKeyImpl(SecurityIdentifier secIdentifier) {
    if(secIdentifier == null) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<SecurityIdentifier>();
      _identifiers.add(secIdentifier);
    }
  }

  @Override
  public Collection<SecurityIdentifier> getIdentifiers() {
    return _identifiers;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SecurityKeyImpl other = (SecurityKeyImpl) obj;
    if (_identifiers == null) {
      if (other._identifiers != null)
        return false;
    } else if (!_identifiers.equals(other._identifiers))
      return false;
    return true;
  }


}
