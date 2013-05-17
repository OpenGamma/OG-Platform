/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

/**
 * 
 */
public class InitialMarginDefinition {

  private final double _confidenceLevel;

  private final int _liquidityHorizon;

  public InitialMarginDefinition(final double confidenceLevel, final int liquidityHorizn) {

    _confidenceLevel = confidenceLevel;

    _liquidityHorizon = liquidityHorizn;

  }

}
