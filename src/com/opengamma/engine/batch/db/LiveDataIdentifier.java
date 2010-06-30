/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.Identifier;

/**
 * 
 */
public class LiveDataIdentifier {
  
  private int _id;
  private String _scheme;
  private String _value;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public String getScheme() {
    return _scheme;
  }
  
  public void setScheme(String scheme) {
    _scheme = scheme;
  }
  
  public String getValue() {
    return _value;
  }
  
  public void setValue(String value) {
    _value = value;
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
  
  public Identifier toOpenGammaIdentifier() {
    return new Identifier(getScheme(), getValue());
  }
  
  
}
