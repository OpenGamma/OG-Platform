/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class OpenGammaVersion {
  
  private int _id;
  private String _version;
  private String _hash;
  
  public int getId() {
    return _id;
  }
  public void setId(int id) {
    _id = id;
  }
  public String getVersion() {
    return _version;
  }
  public void setVersion(String version) {
    _version = version;
  }
  public String getHash() {
    return _hash;
  }
  public void setHash(String hash) {
    _hash = hash;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(_id).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    OpenGammaVersion rhs = (OpenGammaVersion) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
 
}
