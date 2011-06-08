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
 * A live data value.
 * <p>
 * This class is non-modifiable, however a subclass might not be.
 */
public class LiveDataValue {

  /**
   * The specification of what the data represents.
   * This could be a security, such as AAPL stock, or a primitive, such as the EUR/USD exchange rate.
   */
  private ComputationTargetSpecification _computationTargetSpecification;
  /**
   * The name key of the field, such as 'MARKET_VALUE'.
   */
  private String _fieldName;
  /**
   * The value, held as a {@code double}.
   */
  private double _value;

  /**
   * Creates an instance from a {@code ComputedValue}.
   * 
   * @param value  the computed value, not null
   */
  public LiveDataValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "value");
    _computationTargetSpecification = value.getSpecification().getTargetSpecification();
    _fieldName = value.getSpecification().getValueName();
    
    if (!(value.getValue() instanceof Double)) {
      throw new IllegalArgumentException("Value must be a Double, was " + value.getValue().getClass());      
    }
    _value = (Double) value.getValue();
  }

  /**
   * Creates an instance from a {@code ValueSpecification} and value.
   * 
   * @param valueSpecification  the value specification, not null
   * @param value  the value, not null
   */
  public LiveDataValue(
      ValueSpecification valueSpecification,
      Double value) {
    ArgumentChecker.notNull(valueSpecification, "specification");
    ArgumentChecker.notNull(value, "value");
    _computationTargetSpecification = valueSpecification.getTargetSpecification();
    _fieldName = valueSpecification.getValueName();
    _value = value;
  }

  /**
   * Creates an instance from a {@code ValueRequirement} and alue.
   * 
   * @param valueRequirement  the value requirement, not null
   * @param value  the value, not null
   */
  public LiveDataValue(
      ValueRequirement valueRequirement,
      Double value) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    ArgumentChecker.notNull(value, "Value");
    _computationTargetSpecification = valueRequirement.getTargetSpecification();
    _fieldName = valueRequirement.getValueName();
    _value = value;
  }

  /**
   * Creates an instance from a {@code ComputationTargetSpecification}, field name and value.
   * 
   * @param computationTargetSpecification  the target specification, not null
   * @param fieldName  the field name, not null
   * @param value  the value, not null
   */
  public LiveDataValue(
      ComputationTargetSpecification computationTargetSpecification,
      String fieldName,
      double value) {
    ArgumentChecker.notNull(computationTargetSpecification, "computationTargetSpecification");
    ArgumentChecker.notNull(fieldName, "fieldName");
    _computationTargetSpecification = computationTargetSpecification;
    _fieldName = fieldName;
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the computation target specification.
   * 
   * @return the specification, not null
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  /**
   * Gets the field name key defining the value.
   * 
   * @return the field name, not null
   */
  public String getFieldName() {
    return _fieldName;
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public double getValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
