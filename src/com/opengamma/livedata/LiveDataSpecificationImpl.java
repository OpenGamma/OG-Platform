/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiersImpl;

/**
 * A simple implementation of {@link LiveDataSpecification}.
 *
 * @author kirk
 */
public class LiveDataSpecificationImpl
extends DomainSpecificIdentifiersImpl
implements LiveDataSpecification {
  public LiveDataSpecificationImpl(DomainSpecificIdentifier... identifiers) {
    super(identifiers);
  }
  
  public LiveDataSpecificationImpl(Collection<? extends DomainSpecificIdentifier> identifiers) {
    super(identifiers);
  }
  
  public LiveDataSpecificationImpl(DomainSpecificIdentifier identifier) {
    super(identifier);
  }

}
