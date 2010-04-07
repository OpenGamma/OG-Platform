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
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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
public class ComputedValue implements Serializable {
  private static final String SPECIFICATION_KEY = "specification";
  private static final String VALUE_KEY = "value";
  
  private final ValueSpecification _specification;
  private final Object _value;
  
  public ComputedValue(ValueSpecification specification, Object value) {
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
    if(!(obj instanceof ComputedValue)) {
      return false;
    }
    ComputedValue other = (ComputedValue) obj;
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
  
  public FudgeFieldContainer toFudgeMsg (final FudgeSerializationContext context) {
    MutableFudgeFieldContainer message = context.newMessage ();
    message.add (SPECIFICATION_KEY, getSpecification ().toFudgeMsg (context));
    context.objectToFudgeMsg (message, VALUE_KEY, null, getValue ());
    return message;
  }
  
  public static ComputedValue fromFudgeMsg (final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    ValueSpecification valueSpec = context.fieldValueToObject(ValueSpecification.class, message.getByName (SPECIFICATION_KEY));
    Object valueObject = context.fieldValueToObject (message.getByName (VALUE_KEY));
    return new ComputedValue(valueSpec, valueObject);
  }
  
}
