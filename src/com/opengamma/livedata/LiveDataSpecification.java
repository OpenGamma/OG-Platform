/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;

public class LiveDataSpecification
extends DomainSpecificIdentifiers {
  
  public LiveDataSpecification(LiveDataSpecification source) {
    this(source.getIdentifiers());        
  }
  
  public LiveDataSpecification(DomainSpecificIdentifier... identifiers) {
    super(identifiers);
  }
  
  public LiveDataSpecification(Collection<? extends DomainSpecificIdentifier> identifiers) {
    super(identifiers);
  }
  
  public LiveDataSpecification(DomainSpecificIdentifier identifier) {
    super(identifier);
  }
  
  public LiveDataSpecification(FudgeFieldContainer fudgeMsg) {
    super(fudgeMsg);
  }

}
