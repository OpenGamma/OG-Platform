/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

/**
 * Enumerate the different types of asset classes (principally for use in modelling a CCP)
 */
public enum AssetClasses {
  /**
   * Single name products e.g. SNCDS, SNCDS Swaptions, CLN's
   */
  CREDIT_SINGLENAME,
  /**
   * CDS indices e.g. CDX
   */
  CREDIT_INDEX,
  /**
   * Multi-name products e.g. default baskets, synthetic CDO's
   */
  CREDIT_CORRELATION,
  /**
   * 
   */
  COMMODITIES,
  /**
   * 
   */
  EQUITY,
  /**
   * 
   */
  FX,
  /**
   * 
   */
  RATES,
  /**
   * 
   */
  SECURITISED;
}
