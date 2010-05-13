/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import java.util.Set;

import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.UnderlyingType;

public class ValueGreekSensitivity implements Sensitivity<ValueGreek> {
  private final ValueGreek _valueGreek;
  private final String _identifier;

  public ValueGreekSensitivity(final ValueGreek valueGreek, final String identifier) {
    _valueGreek = valueGreek;
    _identifier = identifier;
  }

  @Override
  public String getIdentifier() {
    return _identifier;
  }

  @Override
  public ValueGreek getSensitivity() {
    return _valueGreek;
  }

  public int getOrder() {
    return _valueGreek.getUnderlyingGreek().getUnderlying().getOrder();
  }

  public Set<UnderlyingType> getUnderlyings() {
    return _valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings();
  }

  public Underlying getUnderlying() {
    return _valueGreek.getUnderlyingGreek().getUnderlying();
  }

  @Override
  public String toString() {
    return "[" + _valueGreek.toString() + ", " + _identifier + "]";
  }
}
