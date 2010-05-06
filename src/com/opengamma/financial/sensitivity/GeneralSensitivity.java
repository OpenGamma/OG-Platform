/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.Order;

public class GeneralSensitivity extends Sensitivity<String> {
  //TODO: temporarily a String

  public GeneralSensitivity(final String underlying, final Order order) {
    super(underlying, order);
  }

}
