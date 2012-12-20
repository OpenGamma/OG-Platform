/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForwardSwapCurveSpecification extends ForwardCurveSpecification {

  public ForwardSwapCurveSpecification(final String name, final Currency target, final ForwardCurveInstrumentProvider curveInstrumentProvider) {
    super(name, target, curveInstrumentProvider);
  }

}
