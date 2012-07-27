/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import com.opengamma.financial.analytics.model.InstrumentTypeProperties;

/**
 * 
 */
public class BondFuturePriceCurveFunction extends FuturePriceCurveFunction {

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.BOND_FUTURE_PRICE;
  }

}
