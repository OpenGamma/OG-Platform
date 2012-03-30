/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.sensitivity;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;

/**
 * 
 */
public class PositionGreek {
  private final Greek _underlyingGreek;

  public PositionGreek(final Greek underlyingGreek) {
    Validate.notNull(underlyingGreek, "underlying greek");
    _underlyingGreek = underlyingGreek;
  }

  public Greek getUnderlyingGreek() {
    return _underlyingGreek;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_underlyingGreek == null) ? 0 : _underlyingGreek.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PositionGreek other = (PositionGreek) obj;
    return ObjectUtils.equals(_underlyingGreek, other._underlyingGreek);
  }

  @Override
  public String toString() {
    return "POSITION_" + _underlyingGreek.toString();
  }
}
