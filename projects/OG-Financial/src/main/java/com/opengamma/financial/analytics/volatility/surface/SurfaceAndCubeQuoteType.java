/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

/**
 *
 */
public class SurfaceAndCubeQuoteType {
  /** Surfaces or cubes defined using call options and deltas */
  public static final String CALL_DELTA = "CallDelta";
  /** Surfaces or cubes defined using put options and deltas */
  public static final String PUT_DELTA = "PutDelta";
  /** Surfaces or cubes defined using call options and strike */
  public static final String CALL_STRIKE = "CallStrike";
  /** Surfaces or cubes defined using call and put options and strike */
  public static final String CALL_AND_PUT_STRIKE = "CallPutStrike";
  /** Surfaces or cubes defined using put options and strike */
  public static final String PUT_STRIKE = "PutStrike";
  /** Surfaces or cubes defined using pay/receive deltas */
  public static final String PAY_RECEIVE_DELTA = "PayReceiveDelta";
  /** Surfaces or cubes defined using strangles and risk reversals */
  public static final String MARKET_STRANGLE_RISK_REVERSAL = "MarketStrangleRiskReversal";
  /** Surfaces or cubes defined using expiry / maturity ATM values (useful for swaptions) */
  public static final String EXPIRY_MATURITY_ATM = "ExpiryMaturityATM";
  /** Surfaces or cubes defined using relative (to the forward) strikes */
  public static final String RELATIVE_STRIKE = "RelativeStrike";
  /** Surfaces or cubes defined as flat (i.e. no smile) with a term structure */
  public static final String FLAT_WITH_TERM_STRUCTURE = "FlatWithTermStructure";
}
