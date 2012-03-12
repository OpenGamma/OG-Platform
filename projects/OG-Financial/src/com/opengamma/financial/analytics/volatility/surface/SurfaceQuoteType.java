/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

/**
 * 
 */
public class SurfaceQuoteType {
  /** Surfaces defined using call options and deltas */
  public static final String CALL_DELTA = "CallDelta";
  /** Surfaces defined using put options and deltas */
  public static final String PUT_DELTA = "PutDelta";
  /** Surfaces defined using call options and strike */
  public static final String CALL_STRIKE = "CallStrike";
  /** Surfaces defined using call and put options and strike */
  public static final String CALL_AND_PUT_STRIKE = "CallPutStrike";
  /** Surfaces defined using put options and strike */
  public static final String PUT_STRIKE = "PutStrike";
  /** Surfaces defined using pay/receive deltas */
  public static final String PAY_RECEIVE_DELTA = "PayReceiveDelta";
  /** Surfaces defined using strangles and risk reversals */
  public static final String MARKET_STRANGLE_RISK_REVERSAL = "MarketStrangleRiskReversal";
}
