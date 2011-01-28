/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
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
  
  public LiveDataValue(
      ComputedValue value) {
    ArgumentChecker.notNull(value, "Value");
    
    _computationTargetSpecification = value.getSpecification().getTargetSpecification();
    _fieldName = value.getSpecification().getValueName();
    
    if (!(value.getValue() instanceof Double)) {
      throw new IllegalArgumentException("Value must be a Double, was " + value.getValue().getClass());      
    }
    _value = (Double) value.getValue();
  }
  
  public LiveDataValue(
      ValueSpecification specification,
      Double value) {
    ArgumentChecker.notNull(specification, "Value specification");
    ArgumentChecker.notNull(value, "Value");
    
    _computationTargetSpecification = specification.getTargetSpecification();
    _fieldName = specification.getValueName();
    _value = (Double) value;
  }
  
  public LiveDataValue(
      ValueRequirement requirement,
      Double value) {
    ArgumentChecker.notNull(requirement, "Value requirement");
    ArgumentChecker.notNull(value, "Value");
    
    _computationTargetSpecification = requirement.getTargetSpecification();
    _fieldName = requirement.getValueName();
    _value = (Double) value;
  }
  
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
