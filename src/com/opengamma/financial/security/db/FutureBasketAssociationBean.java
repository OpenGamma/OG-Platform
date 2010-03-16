/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

/**
 * 
 *
 * @author Andrew Griffin
 */
public class FutureBasketAssociationBean {
  private Long _id = null;
  private FutureSecurityBean _futureSecurity = null;
  private DomainSpecificIdentifierBean _domainSpecificIdentifier = null;

  public FutureBasketAssociationBean() {
  }
  
  public FutureBasketAssociationBean(FutureSecurityBean futureSecurity, DomainSpecificIdentifierBean domainSpecificIdentifier) {
    _futureSecurity = futureSecurity;
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
  
  public FutureSecurityBean getFutureSecurity() {
    return _futureSecurity;
  }
  
  public void setFutureSecurity(FutureSecurityBean futureSecurity) {
    _futureSecurity = futureSecurity;
  }
  
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof FutureBasketAssociationBean)) {
      return false;
    }
    FutureBasketAssociationBean otherBean = (FutureBasketAssociationBean) other;
    return (getFutureSecurity ().equals (otherBean.getFutureSecurity ())
     && getDomainSpecificIdentifier ().equals (otherBean.getDomainSpecificIdentifier ()));
  }
  
}
