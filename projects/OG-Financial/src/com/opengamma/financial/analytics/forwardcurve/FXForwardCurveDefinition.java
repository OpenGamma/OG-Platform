/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXForwardCurveDefinition extends ForwardCurveDefinition {

  public FXForwardCurveDefinition(final String name, final UnorderedCurrencyPair target, final Tenor[] tenors) {
    super(name, target, tenors);
  }
}
