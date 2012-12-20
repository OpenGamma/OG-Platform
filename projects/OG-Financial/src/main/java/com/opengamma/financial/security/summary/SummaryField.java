/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

/**
 * Enumerates a set of common fields designed to access summary information for a security. A degree of overloading
 * is required to apply these general fields across the security types, and not all fields will apply to all security
 * types.
 */
public enum SummaryField {

  /**
   * The security type
   */
  TYPE,
  /**
   * A description of the security
   */
  DESCRIPTION,
  /**
   * The start date
   */
  START,
  /**
   * The expiry or maturity date
   */
  MATURITY,
  /**
   * The notional
   */
  NOTIONAL,
  /**
   * The rate or strike
   */
  STRIKE,
  /**
   * The frequency, e.g. payment frequency
   */
  FREQUENCY,
  /**
   * The direction (e.g. put/call, pay/receive)
   */
  DIRECTION,
  /**
   * The underlying
   */
  UNDERLYING
  
}
