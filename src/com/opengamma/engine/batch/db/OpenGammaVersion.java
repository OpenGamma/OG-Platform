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
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
 
}
