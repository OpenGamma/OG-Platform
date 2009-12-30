/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

// NOTE kirk 2009-12-30 -- This is VERY intentionally NOT generified. Having actually
// worked with the old version of ComputedValue with generics in the engine, the number
// of warning suppressions necessary just to get it working wasn't matched by any
// type of benefit in the additional type information at compile time.

// REVIEW kirk 2009-12-30 -- I've intentionally not made this an interface as we
// never actually made use of that functionality. We'll split it if and when we have
// a compelling reason to do so.

/**
 * 
 *
 * @author kirk
 */
public class NewComputedValue implements Serializable {
  private final ValueSpecification _specification;
  private final Object _value;
  
  public NewComputedValue(ValueSpecification specification, Object value) {
    ArgumentChecker.checkNotNull(specification, "Value Specification");
    ArgumentChecker.checkNotNull(value, "Value; for uncomputed values use a standard nonce value.");
    _specification = specification;
    _value = value;
  }

  /**
   * @return the specification
   */
  public ValueSpecification getSpecification() {
    return _specification;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof NewComputedValue)) {
      return false;
    }
    NewComputedValue other = (NewComputedValue) obj;
    if(!ObjectUtils.equals(_specification, other._specification)) {
      return false;
    }
    if(!ObjectUtils.equals(_value, other._value)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    result = (result * prime) + getSpecification().hashCode();
    result = (result * prime) + getValue().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
