/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

/**
 * Temporary enum until we replace these with proper securities
 */
public enum StripInstrumentType {
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
  SWAP,
  /**
   * Tenor swap
   */
  TENOR_SWAP,
  /**
   * Basis swap
   */
  BASIS_SWAP
}
