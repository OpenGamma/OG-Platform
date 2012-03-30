/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.sensitivity;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;

/**
 * 
 */
public class ValueGreekSensitivity implements Sensitivity<ValueGreek> {
  private final ValueGreek _valueGreek;
  private final String _identifier;

  public ValueGreekSensitivity(final ValueGreek valueGreek, final String identifier) {
    Validate.notNull(valueGreek, "ValueGreek");
    Validate.notNull(identifier, "identifier");
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

  @Override
  public int getOrder() {
    return _valueGreek.getUnderlyingGreek().getUnderlying().getOrder();
  }

  @Override
  public List<UnderlyingType> getUnderlyingTypes() {
    return _valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings();
  }

  @Override
  public Underlying getUnderlying() {
    return _valueGreek.getUnderlyingGreek().getUnderlying();
  }

  @Override
  public String toString() {
    return "[" + _valueGreek.toString() + ", " + _identifier + "]";
  }

  // hashCode() and equals() deliberately not overridden
}
