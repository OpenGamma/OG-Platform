/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Hibernate bean for storing UniqueId.
 */
public class UniqueIdBean {
  // Note: the reason that this doesn't have an id is that it's a hibernate
  // component of other beans so it doesn't need one
  // Note: misleadingly, this only stores the scheme and value, not the version
  private String _identifier;
  private String _scheme;
  
  public UniqueIdBean() {
  }

  public UniqueIdBean(String scheme, String identifier) {
    _scheme = scheme;
    _identifier = identifier;
  }

  public String getScheme() {
    return _scheme;
  }
  
  public void setScheme(String scheme) {
    _scheme = scheme;
  }
  
  public String getIdentifier() {
    return _identifier;
  }
  
  public void setIdentifier(String identifier) {
    _identifier = identifier;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_identifier == null) ? 0 : _identifier.hashCode());
    result = prime * result + ((_scheme == null) ? 0 : _scheme.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) { 
      return true;
    }
    if (!(other instanceof UniqueIdBean)) {
      return false;
    }
    UniqueIdBean otherBean = (UniqueIdBean) other;
    return ObjectUtils.equals(otherBean.getScheme(), getScheme())
        && ObjectUtils.equals(otherBean.getIdentifier(), getIdentifier());
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
