/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LiveDataValue {
  
  /**
   * For example, BLOOMBERG_BUID:EQ1251521510000.
   */
  private Identifier _identifier;
  
  /**
   * For example, IndicativeValue
   */
  private String _fieldName;
  
  /**
   * For example, 55.02
   */
  private double _value;
  
  public LiveDataValue(Identifier identifier,
      String fieldName,
      double value) {
    ArgumentChecker.notNull(identifier, "Identifier");
    ArgumentChecker.notNull(fieldName, "Field name");
    
    _identifier = identifier;
    _fieldName = fieldName;
    _value = value;
  }

  public Identifier getIdentifier() {
    return _identifier;
  }

  public void setIdentifier(Identifier identifier) {
    _identifier = identifier;
  }

  public String getFieldName() {
    return _fieldName;
  }

  public void setFieldName(String fieldName) {
    _fieldName = fieldName;
  }

  public double getValue() {
    return _value;
  }

  public void setValue(double value) {
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

}
