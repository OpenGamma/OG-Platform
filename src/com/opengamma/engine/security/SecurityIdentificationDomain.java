/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A typesafe wrapper on top of a name for a security identification domain.
 *
 * @author kirk
 */
public class SecurityIdentificationDomain implements Serializable, Cloneable {
  private final String _domainName;
  
  public SecurityIdentificationDomain(String domainName) {
    if(domainName == null) {
      throw new NullPointerException("Must name this domain.");
    }
    _domainName = domainName;
  }

  /**
   * @return the domainName
   */
  public String getDomainName() {
    return _domainName;
  }

  @Override
  protected SecurityIdentificationDomain clone() {
    try {
      return (SecurityIdentificationDomain) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Cloning actually IS supported");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof SecurityIdentificationDomain)) {
      return false;
    }
    SecurityIdentificationDomain other = (SecurityIdentificationDomain) obj;
    if(!ObjectUtils.equals(_domainName, other._domainName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _domainName.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
}
