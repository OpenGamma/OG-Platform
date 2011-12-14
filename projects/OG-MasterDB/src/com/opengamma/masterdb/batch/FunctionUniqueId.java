/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Hibernate bean.
 */
public class FunctionUniqueId {
  
  private long _id;
  private String _uniqueId;
  
  public long getId() {
    return _id;
  }
 
  public void setId(long id) {
    _id = id;
  }
  
  public String getUniqueId() {
    return _uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    _uniqueId = uniqueId;
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
