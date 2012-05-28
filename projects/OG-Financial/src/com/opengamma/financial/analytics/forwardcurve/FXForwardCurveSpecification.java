/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.util.money.UnorderedCurrencyPair;


/**
 * 
 */
public class FXForwardCurveSpecification extends ForwardCurveSpecification {

  public FXForwardCurveSpecification(final String name, final UnorderedCurrencyPair target, final ForwardCurveInstrumentProvider curveInstrumentProvider) {
    super(name, target, curveInstrumentProvider);
  }

}
