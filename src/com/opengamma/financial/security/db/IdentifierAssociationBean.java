/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 *
 * @author jim
 */
public class IdentifierAssociationBean {
  private Long _id = null;
  private SecurityBean _security = null;
  private IdentifierBean _identifier = null;
  private Date _validStartDate = null; // inclusive
  private Date _validEndDate = null; // not inclusive

  public IdentifierAssociationBean() {
  }
  
  public IdentifierAssociationBean(SecurityBean security, IdentifierBean identifier) {
    _security = security;
    _identifier = identifier;
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public IdentifierBean getIdentifier() {
    return _identifier;
  }
  
  public void setIdentifier(IdentifierBean identifier) {
    _identifier = identifier;
  }
  
  public SecurityBean getSecurity() {
    return _security;
  }
  
  public void setSecurity(SecurityBean security) {
    _security = security;
  }
  
  public Date getValidStartDate () {
    return _validStartDate;
  }
  
  public void setValidStartDate (final Date validStartDate) {
    _validStartDate = validStartDate;
  }
  
  public Date getValidEndDate () {
    return _validEndDate;
  }
  
  public void setValidEndDate (final Date validEndDate) {
    _validEndDate = validEndDate;
  }
  
  // note this will match objects with different id's as long as the domain and identifier are the same.
  public boolean equals(Object other) {
    if (!(other instanceof IdentifierAssociationBean)) {
      return false;
    }
    IdentifierAssociationBean otherBean = (IdentifierAssociationBean) other;
    if (ObjectUtils.equals(otherBean.getId(), getId())) {
      return true;
    }
    if (ObjectUtils.equals(otherBean.getSecurity(), getSecurity()) &&
        ObjectUtils.equals(otherBean.getIdentifier(), getIdentifier()) &&
        ObjectUtils.equals(otherBean.getValidStartDate(), getValidStartDate()) &&
        ObjectUtils.equals(otherBean.getValidStartDate(), getValidStartDate())) {
      return true;
    }
    return false;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
