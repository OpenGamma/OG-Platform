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
  public static final String PROPERTY_SURFACE_QUOTE_TYPE = "SurfaceQuoteType";
  public static final String CALL_DELTA = "CallDelta";
  public static final String PUT_DELTA = "PutDelta";
  public static final String CALL_STRIKE = "CallStrike";
  public static final String STRIKE_CALL_AND_PUT = "CallPutStrike";
  public static final String PUT_STRIKE = "PutStrike";
  public static final String PAY_RECEIVE_DELTA = "PayReceiveDelta";
  public static final String MARKET_STRANGLE_RISK_REVERSAL = "MarketStrangleRiskReversal";
}
