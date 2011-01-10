/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates the logic for deciding whether the difference between any two {@link ComputedValue}s is sufficient to
 * be treated as a delta (in the context of a change). In the absence of a specific comparer, the implementation will
 * fall back onto {@link ObjectUtils#equals(Object)}.
 */
@PublicAPI
public class DeltaDefinition {
  
  private DeltaComparer<Number> _numberComparer;
  
  /**
   * Sets a {@link DeltaComparer} to be used for numbers.
   * 
   * @param numberComparer  the comparer to use for numbers.
   */
  public void setNumberComparer(DeltaComparer<Number> numberComparer) {
    _numberComparer = numberComparer;
  }

  /**
   * @return  the comparer being used for numbers.
   */
  public DeltaComparer<Number> getNumberComparer() {
    return _numberComparer;
  }

  public boolean isDelta(ComputedValue previousComputed, ComputedValue newComputed) {
    if (previousComputed == null && newComputed == null) {
      return false;
    }
    if (previousComputed == null || newComputed == null) {
      return true;
    }
    if (!ObjectUtils.equals(previousComputed.getSpecification(), newComputed.getSpecification())) {
      // At least the specifications differ, which we want to report as a delta.
      return true;
    }

    // REVIEW jonathan 2010-05-10 -- Written with the assumption that we only really want to compare doubles and
    // BigDecimals, hence the specific Number check here rather than anything more generic.
    Object previousValue = previousComputed.getValue();
    Object newValue = newComputed.getValue();
    if (getNumberComparer() != null && previousValue instanceof Number && newValue instanceof Number) {
      return getNumberComparer().isDelta((Number) previousValue, (Number) newValue);
    }
    
    // Finally, fall back onto the most basic check
    return !ObjectUtils.equals(previousValue, newValue);
  }
  
  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(_numberComparer);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DeltaDefinition)) {
      return false;
    }
    
    DeltaDefinition other = (DeltaDefinition) obj;
    if (getNumberComparer() == null) {
      return other.getNumberComparer() == null;
    }
    return getNumberComparer().equals(other.getNumberComparer());
  }
  
}
