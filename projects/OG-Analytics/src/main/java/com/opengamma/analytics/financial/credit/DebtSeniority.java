/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * The seniority of the debt of the reference entity
 * 
 * @deprecated use com.opengamma.core.legalentity.SeniorityLevel
 */
@Deprecated
public enum DebtSeniority {
  /**
   * Senior debt
   */
  SENIOR,
  /**
   * Sub-ordinate debt
   */
  SUBORDINATED,
  /**
   * Junior Subordinated or Upper Tier 2 Debt (Banks) - MarkIt notation
   */
  JRSUBUT2,
  /**
   * Preference Shares or Tier 1 Capital (Banks) - MarkIt notation
   */
  PREFT1,
  /**
   * Secured Debt (Corporate/Financial) or Domestic Currency Sovereign Debt (Government) - MarkIt notation
   */
  SECDOM,
  /**
   * Senior Unsecured Debt (Corporate/Financial), Foreign Currency Sovereign Debt (Government) - MarkIt notation
   */
  SNRFOR,
  /**
   * Subordinated or Lower Tier 2 Debt (Banks) - MarkIt notation
   */
  SUBLT2,
  /**
   * 
   */
  NONE;
}
