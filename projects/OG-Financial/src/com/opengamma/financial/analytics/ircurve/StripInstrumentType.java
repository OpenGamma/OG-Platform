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
  /** CDOR */
  CDOR,
  /** Cibor */
  CIBOR,
  /** Stibor */
  STIBOR,
  /** Zero deposit strip */
  ZERO_DEPOSIT
}
