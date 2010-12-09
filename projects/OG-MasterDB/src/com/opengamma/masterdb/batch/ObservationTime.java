/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class ObservationTime {
  
  private int _id;
  private String _label;
  
  public int getId() {
    return _id;
  }
 
  public void setId(int id) {
    _id = id;
  }
  
  public String getLabel() {
    return _label;
  }
  
  public void setLabel(String label) {
    _label = label;
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
