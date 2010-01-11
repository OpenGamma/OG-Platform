/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 *
 * @author jim
 */
public class DomainSpecificIdentifierBean {
  // Note: the reason that this doesn't have an id is that it's a hibernate
  // component of DomainSpecificIndentifierAssociationBean so it doesn't need one
  private String _identifier;
  private String _domain;
  
  public DomainSpecificIdentifierBean() {
  }

  public DomainSpecificIdentifierBean(String domain, String identifier) {
    _domain = domain;
    _identifier = identifier;
  }
  
  public String getDomain() {
    return _domain;
  }
  
  public void setDomain(String domain) {
    _domain = domain;
  }
  
  public String getIdentifier() {
    return _identifier;
  }
  
  public void setIdentifier(String identifier) {
    _identifier = identifier;
  }
  
  // note this will match objects with different id's as long as the domain and identifier are the same.
  public boolean equals(Object other) {
    if (!(other instanceof DomainSpecificIdentifierBean)) {
      return false;
    }
    DomainSpecificIdentifierBean otherBean = (DomainSpecificIdentifierBean) other;
    if (ObjectUtils.equals(otherBean.getDomain(), getDomain()) &&
        ObjectUtils.equals(otherBean.getIdentifier(), getIdentifier())) {
      return true;
    }
    return false;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
