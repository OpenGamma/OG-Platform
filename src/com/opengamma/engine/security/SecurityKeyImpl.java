/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiersImpl;

// REVIEW kirk 2009-09-01 -- I'm not particularly happy with the name of this class.

/**
 * A concrete, immutable implementation of {@link SecurityKey}. 
 *
 * @author kirk
 */
public class SecurityKeyImpl
extends DomainSpecificIdentifiersImpl
implements SecurityKey {
  public SecurityKeyImpl(DomainSpecificIdentifier... identifiers) {
    super(identifiers);
  }
  
  public SecurityKeyImpl(Collection<? extends DomainSpecificIdentifier> identifiers) {
    super(identifiers);
  }
  
  public SecurityKeyImpl(DomainSpecificIdentifier secIdentifier) {
    super(secIdentifier);
  }

}
