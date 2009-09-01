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
public class DefaultSecurityKey implements SecurityKey, Serializable {
  private final List<SecurityIdentifier> _identifiers;
  
  public DefaultSecurityKey(SecurityIdentifier... identifiers) {
    if((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<SecurityIdentifier>(identifiers.length);
      for(SecurityIdentifier secId : identifiers) {
        _identifiers.add(secId);
      }
    }
  }
  
  public DefaultSecurityKey(Collection<? extends SecurityIdentifier> identifiers) {
    if(identifiers == null) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<SecurityIdentifier>(identifiers);
    }
  }
  
  public DefaultSecurityKey(SecurityIdentifier secIdentifier) {
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

}
