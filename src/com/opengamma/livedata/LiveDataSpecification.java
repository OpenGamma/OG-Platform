/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSpecification {
  
  private final DomainSpecificIdentifiers _domainSpecificIdentifiers;
  
  public LiveDataSpecification(LiveDataSpecification source) {
    this(source.getIdentifiers());        
  }
  
  public LiveDataSpecification(DomainSpecificIdentifier... identifiers) {
    this(new DomainSpecificIdentifiers(identifiers));
  }
  
  public LiveDataSpecification(Collection<? extends DomainSpecificIdentifier> identifiers) {
    this(new DomainSpecificIdentifiers(identifiers));
  }
  
  public LiveDataSpecification(DomainSpecificIdentifier identifier) {
    this(new DomainSpecificIdentifiers(identifier));
  }
  
  public LiveDataSpecification(FudgeFieldContainer fudgeMsg) {
    this(new DomainSpecificIdentifiers(fudgeMsg));
  }
  
  public LiveDataSpecification(DomainSpecificIdentifiers domainSpecificIdentifiers) {
    ArgumentChecker.checkNotNull(domainSpecificIdentifiers, "Identifiers");
    _domainSpecificIdentifiers = domainSpecificIdentifiers;
  }
  
  public DomainSpecificIdentifiers getIdentifiers() {
    return _domainSpecificIdentifiers;
  }
  
  public String getIdentifier(IdentificationDomain domain) {
    return _domainSpecificIdentifiers.getIdentifier(domain);
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    return _domainSpecificIdentifiers.toFudgeMsg(fudgeMessageFactory);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((_domainSpecificIdentifiers == null) ? 0
            : _domainSpecificIdentifiers.hashCode());
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
    LiveDataSpecification other = (LiveDataSpecification) obj;
    if (_domainSpecificIdentifiers == null) {
      if (other._domainSpecificIdentifiers != null)
        return false;
    } else if (!_domainSpecificIdentifiers
        .equals(other._domainSpecificIdentifiers))
      return false;
    return true;
  }
  
}
