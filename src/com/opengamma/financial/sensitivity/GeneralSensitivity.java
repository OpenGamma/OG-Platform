/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.Order;

public class GeneralSensitivity extends Sensitivity<String> {
  //TODO: a String might not be the best way to label this

  public GeneralSensitivity(final String underlying, final Order order) {
    super(underlying, order);
  }

  public GeneralSensitivity(final String underlying, final Order order, final String label) {
    super(underlying, order, label);
  }

  @Override
  public String toString() {
    return "Sensitivity[" + getUnderlying() + (getLabel() == null ? "]" : ", " + getLabel() + "]");
  }
}
