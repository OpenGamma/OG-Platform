/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor.definition;

import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;

/**
 * 
 */
public class Libor extends ForwardRateAgreement {

  public Libor(final double maturity, final double rate, final String liborCurveName) {
    super(0.0, maturity, rate, liborCurveName, liborCurveName);
  }

  public Libor(final double valueTime, final double maturity, final double yearFraction, final double rate, final String liborCurveName) {
    super(valueTime, maturity, valueTime, yearFraction, yearFraction, rate, liborCurveName, liborCurveName);
  }
}
