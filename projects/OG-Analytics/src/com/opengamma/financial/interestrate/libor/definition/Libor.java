/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor.definition;

import com.opengamma.financial.interestrate.cash.definition.Cash;

/**
 * Libor is just a cash loan with a unit amount borrowed on some some trade date (which could be now), and an amount (1+r*t) paid at maturity, where r is the Libor rate and t is the time (in years) 
 * between the trade date and the maturity in some day count convention.  This is just an extension of the Cash class
 */
public class Libor extends Cash {

  public Libor(final double maturity, final double rate, final String liborCurveName) {
    super(maturity, rate, liborCurveName);
  }

  public Libor(final double maturity, final double rate, final double tradeTime, final double yearFraction, final String liborCurveName) {
    super(maturity, rate, tradeTime, yearFraction, liborCurveName);
  }
}
