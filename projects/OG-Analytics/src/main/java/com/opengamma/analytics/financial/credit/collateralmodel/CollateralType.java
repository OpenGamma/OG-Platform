/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

/**
 * Enumerate the types of assets that can be posted as collateral
 */
public enum CollateralType {
  /**
   * Cash
   */
  CASH,
  /**
   * Government Agency Security
   */
  GOVERNMENTAGENCYSECURITY,
  /**
   * Supranational Bonds
   */
  SUPRA,
  /**
   * Covered Bonds
   */
  COVEREDBOND,
  /**
   * Corporate Bond
   */
  CORPORATEBOND,
  /**
   * Letter of Credit
   */
  LETTEROFCREDIT,
  /**
   * Equities
   */
  EQUITY,
  /**
   * Other (unspecified)
   */
  OTHER;
}
