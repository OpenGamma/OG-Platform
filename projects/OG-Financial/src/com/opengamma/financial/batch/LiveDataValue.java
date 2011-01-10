/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LiveDataValue {
  
  /**
   * A security (for example - AAPL stock) or a primitive (for example - EUR/USD exchange rate)
   */
  private ComputationTargetSpecification _computationTargetSpecification;
  
  /**
   * For example, MARKET_VALUE
   */
  private String _fieldName;
  
  /**
   * For example, 55.02
   */
  private double _value;
  
  public LiveDataValue(ComputationTargetSpecification computationTargetSpecification,
      String fieldName,
      double value) {
    ArgumentChecker.notNull(computationTargetSpecification, "Computation target specification");
    ArgumentChecker.notNull(fieldName, "Field name");
    
    _computationTargetSpecification = computationTargetSpecification;
    _fieldName = fieldName;
    _value = value;
  }

  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  public String getFieldName() {
    return _fieldName;
  }

  public double getValue() {
    return _value;
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
