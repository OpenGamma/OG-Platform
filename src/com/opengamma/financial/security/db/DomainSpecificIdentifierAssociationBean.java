/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 *
 * @author jim
 */
public class DomainSpecificIdentifierAssociationBean {
  private Long _id = null;
  private SecurityBean _security = null;
  private DomainSpecificIdentifierBean _domainSpecificIdentifier = null;

  public DomainSpecificIdentifierAssociationBean(SecurityBean security, DomainSpecificIdentifierBean domainSpecificIdentifier) {
    _security = security;
    _domainSpecificIdentifier = domainSpecificIdentifier;
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public DomainSpecificIdentifierBean getDomainSpecificIdentifier() {
    return _domainSpecificIdentifier;
  }
  
  public void setDomainSpecificIdentifier(DomainSpecificIdentifierBean domainSpecificIdentifier) {
    _domainSpecificIdentifier = domainSpecificIdentifier;
  }
  
  public SecurityBean getSecurity() {
    return _security;
  }
  
  public void setSecurity(SecurityBean security) {
    _security = security;
  }
  
  // note this will match objects with different id's as long as the domain and identifier are the same.
  public boolean equals(Object other) {
    if (!(other instanceof DomainSpecificIdentifierAssociationBean)) {
      return false;
    }
    DomainSpecificIdentifierAssociationBean otherBean = (DomainSpecificIdentifierAssociationBean) other;
    if (ObjectUtils.equals(otherBean.getId(), getId())) {
      return true;
    }
    if (ObjectUtils.equals(otherBean.getSecurity(), getSecurity()) &&
        ObjectUtils.equals(otherBean.getDomainSpecificIdentifier(), getDomainSpecificIdentifier())) {
      return true;
    }
    return false;
  }
}
