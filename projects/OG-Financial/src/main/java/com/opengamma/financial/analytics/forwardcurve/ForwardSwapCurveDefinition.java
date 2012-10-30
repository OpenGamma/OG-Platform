/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ForwardSwapCurveDefinition extends ForwardCurveDefinition {

  public ForwardSwapCurveDefinition(final String name, final Currency target, final Tenor[] tenors) {
    super(name, target, tenors);
  }

}
