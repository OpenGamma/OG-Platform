/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

// NOTE kirk 2009-12-30 -- This is VERY intentionally NOT generified. Having actually
// worked with the old version of ComputedValue with generics in the engine, the number
// of warning suppressions necessary just to get it working wasn't matched by any
// type of benefit in the additional type information at compile time.

// REVIEW kirk 2009-12-30 -- I've intentionally not made this an interface as we
// never actually made use of that functionality. We'll split it if and when we have
// a compelling reason to do so.

/**
 * A value computed by the engine.
 */
@PublicAPI
public class ComputedValue implements Serializable {
  /**
   * The specification of the value.
   */
  private final ValueSpecification _specification;
  /**
   * The value itself.
   */
  private final Object _value;

  /**
   * Creates a computed value.
   * @param specification  the specification, not null
   * @param value  the value, not null
   */
  public ComputedValue(ValueSpecification specification, Object value) {
    ArgumentChecker.notNull(specification, "value specification");
    ArgumentChecker.notNull(value, "value");
    if (value instanceof ComputedValue) {
      throw new IllegalArgumentException("Value must not be a ComputedValue instance");
    }
    _specification = specification;
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the specification.
   * @return the specification, not null
   */
  public ValueSpecification getSpecification() {
    return _specification;
  }

  /**
   * Gets the value.
   * @return the value, not null
   */
  public Object getValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputedValue) {
      ComputedValue other = (ComputedValue) obj;
      return ObjectUtils.equals(_specification, other._specification) &&
        ObjectUtils.equals(_value, other._value);
    }
    return false;
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
    // The fields we're interested in are somewhat deeply nested, so pick them out manually rather than reflecting
    ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
    StringBuffer sb = new StringBuffer();
    style.appendStart(sb, this);
    ValueSpecification spec = getSpecification();
    if (spec != null) {
      style.append(sb, "name", spec.getValueName(), null);
      ComputationTargetSpecification targetSpec = spec.getTargetSpecification();
      style.append(sb, "targetId", targetSpec.getIdentifier(), null);
      style.append(sb, "targetType", targetSpec.getType(), null);
      style.append(sb, "properties", spec.getProperties(), null);
    }
    style.append(sb, "value", getValue(), null);
    style.appendEnd(sb, this);
    return sb.toString();
  }
}
