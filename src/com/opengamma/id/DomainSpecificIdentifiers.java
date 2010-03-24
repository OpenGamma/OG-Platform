/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class DomainSpecificIdentifiers implements Serializable {
  public static final String ID_FUDGE_FIELD_NAME = "ID";
  private final Set<DomainSpecificIdentifier> _identifiers;
  private final int _hashCode;
  
  public DomainSpecificIdentifiers(DomainSpecificIdentifier... identifiers) {
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
  
  public DomainSpecificIdentifiers(Collection<? extends DomainSpecificIdentifier> identifiers) {
    if(identifiers == null) {
      _identifiers = Collections.<DomainSpecificIdentifier>emptySet();
    } else {
      _identifiers = new HashSet<DomainSpecificIdentifier>(identifiers);
    }
    _hashCode = calcHashCode();
  }
  
  public DomainSpecificIdentifiers(DomainSpecificIdentifier identifier) {
    if(identifier == null) {
      _identifiers = Collections.<DomainSpecificIdentifier>emptySet();
    } else {
      _identifiers = new HashSet<DomainSpecificIdentifier>();
      _identifiers.add(identifier);
    }
    _hashCode = calcHashCode();
  }
  
  public DomainSpecificIdentifiers(FudgeFieldContainer fudgeMsg) {
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

  public Collection<DomainSpecificIdentifier> getIdentifiers() {
    return Collections.unmodifiableSet(_identifiers);
  }
  
  public String getIdentifier(IdentificationDomain domain) {
    for(DomainSpecificIdentifier identifier : getIdentifiers()) {
      if(ObjectUtils.equals(domain, identifier.getDomain())) {
        return identifier.getValue();
      }
    }
    return null;
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    ArgumentChecker.checkNotNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    for(DomainSpecificIdentifier identifier: getIdentifiers()) {
      msg.add(ID_FUDGE_FIELD_NAME, identifier.toFudgeMsg(fudgeMessageFactory));
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
    DomainSpecificIdentifiers other = (DomainSpecificIdentifiers) obj;
    if(!ObjectUtils.equals(_identifiers, other._identifiers)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName()).append("[");
    List<String> idsAsText = new ArrayList<String>();
    for(DomainSpecificIdentifier identifier : _identifiers) {
      idsAsText.add(identifier.getDomain().getDomainName() + ":" + identifier.getValue());
    }
    sb.append(StringUtils.join(idsAsText, ", "));
    sb.append("]");
    return sb.toString();
  }

}
