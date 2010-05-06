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

}
