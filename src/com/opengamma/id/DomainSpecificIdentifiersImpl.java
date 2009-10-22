/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;


/**
 * 
 *
 * @author kirk
 */
public class DomainSpecificIdentifiersImpl implements Serializable, DomainSpecificIdentifiers {
  public static final String ID_FUDGE_FIELD_NAME = "ID";
  private final Set<DomainSpecificIdentifier> _identifiers;
  private final int _hashCode;
  
  public DomainSpecificIdentifiersImpl(DomainSpecificIdentifier... identifiers) {
    if((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.<DomainSpecificIdentifier>emptySet();
    } else {
      _identifiers = new HashSet<DomainSpecificIdentifier>(identifiers.length);
      for(DomainSpecificIdentifier secId : identifiers) {
        _identifiers.add(secId);
      }
    }
    _hashCode = calcHashCode();
  }
  
  public DomainSpecificIdentifiersImpl(Collection<? extends DomainSpecificIdentifier> identifiers) {
    if(identifiers == null) {
      _identifiers = Collections.<DomainSpecificIdentifier>emptySet();
    } else {
      _identifiers = new HashSet<DomainSpecificIdentifier>(identifiers);
    }
    _hashCode = calcHashCode();
  }
  
  public DomainSpecificIdentifiersImpl(DomainSpecificIdentifier identifier) {
    if(identifier == null) {
      _identifiers = Collections.<DomainSpecificIdentifier>emptySet();
    } else {
      _identifiers = new HashSet<DomainSpecificIdentifier>();
      _identifiers.add(identifier);
    }
    _hashCode = calcHashCode();
  }
  
  public DomainSpecificIdentifiersImpl(FudgeFieldContainer fudgeMsg) {
    Set<DomainSpecificIdentifier> identifiers = new HashSet<DomainSpecificIdentifier>();
    for(FudgeField field : fudgeMsg.getAllByName(ID_FUDGE_FIELD_NAME)) {
      if(!(field.getValue() instanceof FudgeFieldContainer)) {
        throw new IllegalArgumentException("Message provider has field named " + ID_FUDGE_FIELD_NAME + " which doesn't contain a sub-Message");
      }
      DomainSpecificIdentifier identifier = new DomainSpecificIdentifier((FudgeFieldContainer)field.getValue());
      identifiers.add(identifier);
    }
    _identifiers = identifiers;
    _hashCode = calcHashCode();
  }

  @Override
  public Collection<DomainSpecificIdentifier> getIdentifiers() {
    return Collections.unmodifiableSet(_identifiers);
  }
  
  @Override
  public String getIdentifier(IdentificationDomain domain) {
    for(DomainSpecificIdentifier identifier : getIdentifiers()) {
      if(ObjectUtils.equals(domain, identifier.getDomain())) {
        return identifier.getValue();
      }
    }
    return null;
  }
  
  @Override
  public FudgeFieldContainer toFudgeMsg() {
    FudgeMsg msg = new FudgeMsg();
    for(DomainSpecificIdentifier identifier: getIdentifiers()) {
      msg.add(ID_FUDGE_FIELD_NAME, identifier.toFudgeMsg());
    }
    return msg;
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
    DomainSpecificIdentifiersImpl other = (DomainSpecificIdentifiersImpl) obj;
    if(!ObjectUtils.equals(_identifiers, other._identifiers)) {
      return false;
    }
    return true;
  }
  
}
