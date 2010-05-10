/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.Greek;

public class ValueGreek extends Sensitivity<Greek> {

  public ValueGreek(final Greek underlying) {
    super(underlying, underlying.getOrder());
  }

  public ValueGreek(final Greek underlying, final String label) {
    super(underlying, underlying.getOrder(), label);
  }

  @Override
  public String toString() {
    return "Value[" + getUnderlying() + (getLabel() == null ? "]" : ", " + getLabel() + "]");
  }
}
