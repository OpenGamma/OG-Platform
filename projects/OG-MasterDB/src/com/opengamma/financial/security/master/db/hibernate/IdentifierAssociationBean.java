/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Hibernate bean for storing IdentifierAssociation.
 */
public class IdentifierAssociationBean {
  private Long _id;
  private SecurityBean _security;
  private IdentifierBean _identifier;
  private Date _validStartDate; // inclusive
  private Date _validEndDate; // not inclusive

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
  
  public Date getValidStartDate() {
    return _validStartDate;
  }
  
  public void setValidStartDate(final Date validStartDate) {
    _validStartDate = validStartDate;
  }
  
  public Date getValidEndDate() {
    return _validEndDate;
  }
  
  public void setValidEndDate(final Date validEndDate) {
    _validEndDate = validEndDate;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    result = prime * result + ((_security == null) ? 0 : _security.hashCode());
    result = prime * result + ((_identifier == null) ? 0 : _identifier.hashCode());
    result = prime * result + ((_validStartDate == null) ? 0 : _validStartDate.hashCode());
    result = prime * result + ((_validEndDate == null) ? 0 : _validEndDate.hashCode());
    return result;
  }

  //note this will match objects with different id's as long as the domain and identifier are the same.
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
        ObjectUtils.equals(otherBean.getValidEndDate(), getValidEndDate())) {
      return true;
    }
    return false;
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
