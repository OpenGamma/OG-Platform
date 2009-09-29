/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.DomainSpecificIdentifier;

/**
 * A simple implementation of {@link LiveDataSpecification}.
 *
 * @author kirk
 */
public class LiveDataSpecificationImpl
implements LiveDataSpecification, Serializable {
  private final List<DomainSpecificIdentifier> _identifiers;
  private final int _hashCode;
  
  public LiveDataSpecificationImpl(DomainSpecificIdentifier... identifiers) {
    if((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<DomainSpecificIdentifier>(identifiers.length);
      for(DomainSpecificIdentifier secId : identifiers) {
        _identifiers.add(secId);
      }
    }
    _hashCode = calcHashCode();
  }
  
  public LiveDataSpecificationImpl(Collection<? extends DomainSpecificIdentifier> identifiers) {
    if(identifiers == null) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<DomainSpecificIdentifier>(identifiers);
    }
    _hashCode = calcHashCode();
  }
  
  public LiveDataSpecificationImpl(DomainSpecificIdentifier secIdentifier) {
    if(secIdentifier == null) {
      _identifiers = Collections.emptyList();
    } else {
      _identifiers = new ArrayList<DomainSpecificIdentifier>();
      _identifiers.add(secIdentifier);
    }
    _hashCode = calcHashCode();
  }

  @Override
  public Collection<DomainSpecificIdentifier> getIdentifiers() {
    return _identifiers;
  }
  
  protected int calcHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    return result;
  }
  
  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LiveDataSpecificationImpl other = (LiveDataSpecificationImpl) obj;
    if(!ObjectUtils.equals(_identifiers, other._identifiers)) {
      return false;
    }
    return true;
  }


}
