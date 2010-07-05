/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

/**
 * Temporary enum until we replace these with proper securities
 */
public enum StripInstrument {
  /**
   * Libor rate
   */
  LIBOR,
  /**
   * Cash rate
   */
  CASH,
  /**
   * Forward-rate agreement
   */
  FRA,
  /**
   * Interest-rate future
   */
  FUTURE,
  /**
   * Swap rate
   */
  SWAP
}
