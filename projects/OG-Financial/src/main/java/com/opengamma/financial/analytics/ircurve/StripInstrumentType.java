/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

/**
 * Temporary enum until we replace these with proper securities
 */
public enum StripInstrumentType {
  /** Libor rate */
  LIBOR,
  /** Cash rate */
  CASH,
  /** Forward-rate agreement */
  FRA,
  /** Interest-rate future */
  FUTURE,
  /** Bankers acceptance */
  BANKERS_ACCEPTANCE,
  /** Swap rate */
  SWAP,
  /** Tenor swap */
  TENOR_SWAP,
  /** Basis swap */
  BASIS_SWAP,
  /** OIS swap */
  OIS_SWAP,
  /** Euribor rate */
  EURIBOR,
  /** FRA (3m floating tenor) */
  FRA_3M,
  /** FRA (6m floating tenor) */
  FRA_6M,
  /** Fixed / float swap (3m floating leg reset tenor) */
  SWAP_3M,
  /** Fixed / float swap (6m floating leg reset tenor) */
  SWAP_6M,
  /** Fixed / float swap (12m floating leg reset tenor) */
  SWAP_12M,
  /** CDOR */
  CDOR,
  /** Cibor */
  CIBOR,
  /** Stibor */
  STIBOR,
  /** Simple zero deposit strip */
  SIMPLE_ZERO_DEPOSIT,
  /** Periodic zero deposit strip */
  PERIODIC_ZERO_DEPOSIT,
  /** Continuous zero deposit strip */
  CONTINUOUS_ZERO_DEPOSIT,
  /** Spread strip */
  SPREAD,
  /** Fixed / float swap (28 day floating leg reset tenor) */
  SWAP_28D
}
